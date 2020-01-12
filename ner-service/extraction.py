#!/usr/bin/env python

# python -m pip install pip --upgrade
# python -m pip install wheel
# python -m pip install grpcio-tools==1.22.0
# python -m pip install pyfunctional
# python -m pip install spacy==2.1.8
# python -m install ntlk pyfunctional
# python -m spacy download en_core_web_lg
# python -m pip install git+https://github.com/scrapinghub/dateparser.git

from itertools import chain
from spacy.lang.en import English
from typing import List, Optional
from functional import seq
from dateparser import parse as parse_date
from urgency import get_urgency
# import nltk,spacy
import en_core_web_lg
import re
from spacy.tokens.doc import Doc
nlp = en_core_web_lg.load()

sen_nlp = English()
sen_nlp.max_length = 6 * 1000 * 1000 * 1000
sentencizer = sen_nlp.create_pipe("sentencizer")
sen_nlp.add_pipe(sentencizer)


splitters = [
    "but",
    ", then",
    "and then",
] + ["but {} ".format(suf) for suf in ["I", "she", "he", "it", "we", "they"]]

split_reg = re.compile("({})".format("|".join(splitters)))


def sent_tokenize(data: str):
    # TODO - include but (I|it|he|she)
    return seq(sen_nlp(data).sents)\
        .flat_map(lambda sen: split_reg.split(sen.__repr__()))\
        .filter(lambda tok: tok not in splitters)\
        .map(nlp)\
        .to_list()
    # return list(map(lambda sentence: nlp(sentence), chain(*[sen.__repr__().split("but") for sen in sen_nlp(data).sents])))


prec_pron = "-pron-"
prec_suf = [
    "be in",
    "enter",
    "be look"]
prec_phrases = [prec_pron + " " + suf for suf in prec_suf]


def extract_precondition(nlp_sents: List[Doc]) -> List[str]:
    res = []
    for doc in nlp_sents:
        lemmatized = " ".join(seq(doc).map(lambda x: x.lemma_).to_list()).lower()
        if seq(prec_phrases).exists(lambda phrase: phrase in lemmatized):
            res.append(doc.__repr__())
    return res


def extract_priority(nlp_sents: List[Doc]) -> List[str]:
    urg = get_urgency([s.__repr__() for s in nlp_sents])
    return [] if urg is None else [urg]


app_areas = ["user", "admin", "mobile"]
app_suffixes = ["panel", "application", "website", "page", "board", "site"]
app_area_phrases = seq(app_areas).flat_map(lambda area: seq(app_suffixes).map(lambda suffix: "{} {}".format(area, suffix))).to_list()


def extract_part_of_app(nlp_sents: List[Doc]) -> List[str]:
    res = []
    for doc in nlp_sents:
        lemmatized = " ".join(seq(doc).map(lambda x: x.lemma_).to_list()).lower()
        p = seq(app_area_phrases).filter(lambda phrase: phrase in lemmatized).to_list()
        res = res + p
    return list(set(res))


def extract_expected(nlp_sents: List[Doc]) -> List[str]:
    res = []
    words = ["expect", "suppose", "supposed", "thought", "should", "ought"]
    for doc in nlp_sents:
        if seq(doc).exists(lambda tok: tok.lemma_ in words):
            res.append(doc.__repr__())
    return res


def extract_date(nlp_sents: Doc) -> List[str]:
    dates = []
    times = []

    for doc in nlp_sents:

        for tok in doc.ents:
            if (tok.label_ == 'DATE'):
                dates.append(tok.__repr__())
            elif (tok.label_ == 'TIME'):
                times.append(tok.__repr__())

    dates_set = seq(dates).map(date_to_canonical_or_stay).to_set()

    if len(dates_set) == 1 and len(times) > 0:
        res = datetime_to_canon(dates[0] + " " + times[0])
        return [] if res is None else [res]
    else:
        return seq(dates).map(date_to_canonical).filter_not(lambda x: x is None).to_list()


did_not_regex = re.compile("do not (work|run|complete)")


def extract_actual(nlp_sents: Doc):
    res = []
    words = ["fail", "error", "break", "wrong"]
    for doc in nlp_sents:
        lemmatized = " ".join(seq(doc).map(lambda x: x.lemma_).to_list())

        if did_not_regex.match(lemmatized) is not None or seq(words).exists(lambda w: w in lemmatized) or "stop work" in lemmatized:
            res.append(doc.__repr__())
    return res


def date_to_canonical(date: str) -> Optional[str]:
    dt = parse_date(date)
    if dt is not None:
        return dt.date().strftime("%Y-%m-%dT%H:%M")


def date_to_canonical_or_stay(date: str) -> str:
    canon = date_to_canonical(date)
    return date if canon is None else canon


def datetime_to_canon(date: str) -> Optional[str]:
    dt = parse_date(date)
    if dt is not None:
        return dt.strftime("%Y-%m-%dT%H:%M")
