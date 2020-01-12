from concurrent import futures
import grpc
import time
import generated.ner_pb2 as proto
import generated.ner_pb2_grpc as proto_grpc
from logging import Logger
from typing import List
from functional import seq
import extraction
from similarity import Issue, oddly_similar_issues


def results(text: str, computeUrgency: bool) -> List[proto.Extraction]:

    sents = extraction.sent_tokenize(text)

    return seq([
        (proto.ExtractionType.PRECONDITION, extraction.extract_precondition(sents)),
        (proto.ExtractionType.EXPECTED, extraction.extract_expected(sents)),
        (proto.ExtractionType.ATUAL, extraction.extract_actual(sents)),
        (proto.ExtractionType.PRIORITY, extraction.extract_priority(sents) if computeUrgency else []),
        (proto.ExtractionType.ERROR_DATE, extraction.extract_date(sents)),
        (proto.ExtractionType.PART_OF_APP, extraction.extract_part_of_app(sents))
    ]).map(lambda t_texts: proto.Extraction(extractionType=t_texts[0], extracted=t_texts[1])).to_list()


class NerService(proto_grpc.NerServiceServicer):
    def __init__(self, logger: Logger):
        self.logger = logger

    def GetExtractions(self, request, context):
        try:

            return proto.GetExtractionsResponse(
                extractions=results(
                    request.text,
                    True if request.computeUrgency is None else request.computeUrgency))

        except Exception as ex:
            self.logger.exception(ex)
            raise ex

    def GetOddlySimilar(self, request, context):
        try:

            in_question = Issue(title=extraction.nlp(request.inQuestion.title), id=request.inQuestion.id)
            others = seq(request.entries).map(lambda e: Issue(title=extraction.nlp(e.title), id=e.id)).to_list()
            sims = seq(oddly_similar_issues(in_question, others))

            return proto.GetOddlySimilarResponse(
                similar=seq(sims)
                .map(lambda issue: proto.IssueEntry(title=issue.title.__repr__(), id=issue.id)))

        except Exception as ex:
            self.logger.exception(ex)
            raise ex


def serve(port: int, logger: Logger) -> None:
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    proto_grpc.add_NerServiceServicer_to_server(NerService(logger), server)
    server.add_insecure_port("[::]:{}".format(port))
    server.start()
    logger.info("Server started at port {}".format(port))

    try:
        while True:
            time.sleep(24 * 60 * 60)
    except KeyboardInterrupt:
        server.stop(0)
