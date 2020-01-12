FROM python:3.7-slim

RUN apt-get update && apt-get install -y git && apt-get clean
RUN python -m pip install pip --upgrade
RUN python -m pip install wheel

RUN python -m pip install grpcio-tools==1.22.0
RUN python -m pip install pyfunctional
RUN python -m pip install spacy==2.1.8
#RUN python -m pip install nltk
RUN python -m pip install pyfunctional
RUN python -m spacy download en_core_web_lg
RUN python -m pip install git+https://github.com/scrapinghub/dateparser.git
RUN python -m pip install requests

RUN mkdir -p /app/generated

ADD ./bitehack-proto /app/bitehack-proto
ADD *.py /app/

RUN python -m grpc_tools.protoc -I /app/bitehack-proto/ner-service/ --python_out=/app/generated/ --grpc_python_out=/app/generated/ /app/bitehack-proto/ner-service/ner.proto
RUN ls /app/generated | xargs -I {} sed -i 's/import \([a-zA-Z0-9]*\)_pb2/import generated\.\1_pb2/g'  /app/generated/{}

CMD python /app/main.py
