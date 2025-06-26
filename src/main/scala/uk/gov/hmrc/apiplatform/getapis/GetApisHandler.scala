package uk.gov.hmrc.apiplatform.getapis

import java.net.HttpURLConnection.HTTP_OK

import software.amazon.awssdk.services.apigateway.ApiGatewayClient
import software.amazon.awssdk.services.apigateway.model.{GetRestApisRequest, GetRestApisResponse}
import cats.syntax.either._
import com.amazonaws.services.lambda.runtime.Context
import io.circe.Json
import io.circe.generic.semiauto._
import io.circe.parser._
import io.github.mkotsur.aws.handler.Lambda
import io.github.mkotsur.aws.handler.Lambda._
import io.github.mkotsur.aws.proxy._

import uk.gov.hmrc.api_platform_manage_api.AwsApiGatewayClient.awsApiGatewayClient
import uk.gov.hmrc.api_platform_manage_api.utils.ProxiedRequestHandler

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import io.circe.Encoder
import io.circe.Decoder

object GetApisHandler {
  case class Api(id: String, name: String)
  implicit val apiEncoder: Encoder[Api] = deriveEncoder[Api]

  case class GetApisResponse(restApis: Seq[Api])
  implicit val getApisResponseEncoder: Encoder[GetApisResponse] = deriveEncoder[GetApisResponse]

  implicit val requestContextAuthoriserDecoder: Decoder[RequestContextAuthorizer] = deriveDecoder[RequestContextAuthorizer]
  implicit val requestContextDecoder: Decoder[RequestContext] = deriveDecoder[RequestContext]
}

import GetApisHandler._

class GetApisHandler(apiGatewayClient: ApiGatewayClient = ApiGatewayClient.create(), limit: Int = 500) extends Lambda.ApiProxy[Unit, RequestContext, GetApisResponse] {
  override protected def handle(input: ApiProxyRequest[Unit, RequestContext]) =
    Right(ApiProxyResponse.success(Some(getApisResponse())))

  private def getApisResponse(): GetApisResponse = GetApisResponse(getApis(Seq.empty, None))

  @tailrec
  private def getApis(apis: Seq[Api], position: Option[String]): Seq[Api] = {
    val response: GetRestApisResponse = apiGatewayClient.getRestApis(buildRequest(position))
    val moreApis: Seq[Api] = response.items().asScala.map(item => Api(item.id(), item.name())).toSeq
    if (response.position == null) {
      apis ++ moreApis
    } else {
      getApis(apis ++ moreApis, Some(response.position()))
    }
  }

  private def buildRequest(position: Option[String]): GetRestApisRequest = {
    position match {
      case Some(p) => GetRestApisRequest.builder().limit(limit).position(p).build()
      case None => GetRestApisRequest.builder().limit(limit).build()
    }
  }
}
