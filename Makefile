build:
	docker build -t $(TAG) .

test-locally:
	./run_all_tests.sh

test-unit:
	./batect test-unit
