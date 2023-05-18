FROM amazonlinux as dev

WORKDIR /tmp

RUN yum install -y java-devel maven
RUN curl -L https://www.scala-sbt.org/sbt-rpm.repo > /etc/yum.repos.d/sbt-rpm.repo
RUN yum install -y sbt-1.3.13
RUN sbt update

# Copy java code to the container
COPY . /tmp

# Compile the java code
RUN sbt assembly


FROM public.ecr.aws/lambda/java:11 as release

# Copy uber-jar from the build stage
COPY --from=dev /tmp/lambda.jar ${LAMBDA_TASK_ROOT}/lib/lambda.jar


# Set the CMD to your handler (could also be done as a parameter override outside of the Dockerfile)
CMD [ "uk.gov.hmrc.apiplatform.getapis.GetApisHandler::handle" ]