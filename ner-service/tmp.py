#!/usr/bin/env python

samples = {
    """On yesterday at 5 i couldn't complete the transaction. I expected transaction to look as always, but i saw message "something went wrong".  This issue is very important to us.""": {
        "time": "10 jan at 17:00",
        "exptected": "then transaction to look as always",
        "actual": """saw message "something went wrontg" """,
        "importance": "high",
    },
    """I write to report an issue. Trading Panel stopped working Today at 14:00. User "kazik" was affected. The page wouldnt load, i was a little upset. Please fix it when you can. """: {
        "time": "11 jan at 14:00",
        "exptected": "",
        "actual": """the page wouldn't load""",
        "importance": "low",
    },
    """
   Hello!
Yesterday I had some problems on the admin panel. I tried to create a new user.
The app should create the user an return a success message but nothing happened. The "create" button was completely unresponsive.
Can you fix it ASAP? It's very important. Thank you in advance.
    """: {
        "time": "10 jan at 14:00",
        "exptected": "create the user and return success message",
        "actual": """nothing happened""",
        "importance": "high",
    },
}
