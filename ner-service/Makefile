version = 0.12
container = "supernovaunwired/bite-ner"

protocGenerate:
	rm -rf generated
	mkdir -p generated
	python -m grpc_tools.protoc -I ./bitehack-proto/ner-service/ --python_out=./generated/ --grpc_python_out=./generated/ ./bitehack-proto/ner-service/ner.proto
	ls generated | xargs -I {} sed -i 's/import \([a-zA-Z0-9]*\)_pb2/import generated\.\1_pb2/g'  generated/{}


build:
	rm -rf target
	mkdir -p target
	ls | grep -vE '(Makefile|venv|target)'  | xargs -I {} cp -r ./{} ./target/{}
	cd target && docker build . -t $(container):$(version)

publish: build
	docker push $(container):$(version)

rullzit: build publish

run:
	docker run --rm -p 8080:8080 $(container):$(version)

run_sh:
	docker run --rm -it  --entrypoint /bin/sh  $(container):$(version)
