#!/usr/bin/env python

from extraction import nlp

from functional import seq
from collections import namedtuple
from typing import List
import os
Issue = namedtuple("Issue", ["title", "id"])  # title : spacy...Doc , id:str

threshold = float(os.environ["SIMILARITY_THRESHOLD"]) if "SIMILARITY_THRESHOLD" in os.environ else 0.85


def oddly_similar_issues(in_question: Issue, others: List[Issue]) -> List[Issue]:

    similarity_to_in_question = seq(others).map(lambda issue: (issue, issue.title.similarity(in_question.title)))
    return similarity_to_in_question\
        .sorted(lambda issue_similarity: issue_similarity[1], reverse=True)\
        .take_while(lambda tup: tup[1] > threshold)\
        .map(lambda issue_similarity: issue_similarity[0]).to_list()


if __name__ == '__main__':
    toks = ["It is wrong", "It is good"]

    d = nlp(toks[0])
    print()
