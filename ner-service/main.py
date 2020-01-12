#!/usr/bin/env python
import os
import logging
from logging import INFO


from api import serve

if __name__ == "__main__":

    # import api
    # print(api.results("""I write to report an issue. It was on user page. Trading Panel stopped working Today at 14:00. User "kazik" was affected. The page wouldnt load, i was a little upset. Please fix it when you can. """))
    # exit(1)
    logging.basicConfig(
        level=INFO,
        format="%(asctime)s.%(msecs)03d %(levelname)s %(module)s - %(funcName)s: %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    logger = logging.getLogger(__name__)
    logger.info("Starting ner-service...")

    port = os.environ.get("API_PORT")
    port = 8080 if port is None else int(port)

    serve(port, logger)
