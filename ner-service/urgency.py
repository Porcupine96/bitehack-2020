#!/usr/bin/env python


from functional import seq
import os
import requests
import traceback
from typing import List, Optional, Tuple
from functional.streams import Sequence

monkey_model_id = os.environ['MONKEY_MODEL_ID']
monkey_api_key = os.environ['MONKEY_API_KEY']


def get_urgency(sentences: List[str]) -> Optional[str]:
    if len(sentences) > 0:

        urgencies = _get_urgencies(sentences)

        if urgencies.exists(lambda urg: urg[1] == "HIGH"):
            return "HIGH"
        elif urgencies.exists(lambda urg: urg[1] == "LOW"):
            return "LOW"
        else:
            print("no urg for {}".format(sentences))


def _get_urgencies(sentences: List[str]) -> Sequence:  # of str
    try:
        data = {"data": sentences}

        r = requests.post(
            "https://api.monkeylearn.com/v3/classifiers/{}/classify/".format(monkey_model_id),
            headers={
                "Authorization": "Token {}".format(monkey_api_key),
                "Content-Type": "application/json"
            }, json=data)

        return seq(r.json()).map(lambda e: (e["text"], _process_entry(e)))
    except Exception:
        print(traceback.format_exc())
        return [(sen, None) for sen in sentences]


tags = {
    "Not Urgent": "LOW",
    "Urgent": "HIGH"
}


def _process_entry(entry) -> Optional[Tuple[str, float]]:
    try:

        cls = entry["classifications"][0]

        if not entry["error"] and cls["confidence"] > 0.7:

            return tags[cls["tag_name"]]
        else:
            print("error in fetch")
    except Exception:
        print(traceback.format_exc())


if __name__ == '__main__':
    from extraction import sent_tokenize
    txt = " ".join(["It's very urgent."])

    print(_get_urgencies([s.__repr__() for s in sent_tokenize(txt)]))
    # print(get_urgency(txt))
