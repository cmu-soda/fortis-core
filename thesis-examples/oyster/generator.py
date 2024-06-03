import sys

def gen_card_bal(n):
    return f'''
const MAX_BAL = {n}

CARD_BAL = CARD_BAL[MAX_BAL],
CARD_BAL[b:0..MAX_BAL] = (
    when (b < MAX_BAL) rld.card[c:1..(MAX_BAL-b)] -> CARD_BAL[b+c]
    |
    when (b > 0) rcv.card.fin -> CARD_BAL[b-1]
).
'''

def gen_oyster_bal(n):
    return f'''
const MAX_BAL = {n}

OYSTER_BAL = OYSTER_BAL[MAX_BAL],
OYSTER_BAL[b:0..MAX_BAL] = (
    when (b < MAX_BAL) rld.oyster[c:1..(MAX_BAL-b)] -> OYSTER_BAL[b+c]
    |
    when (b > 0) rcv.oyster.fin -> OYSTER_BAL[b-1]
    |
    when (b == 0) snd.oyster.gin -> no_oyster_bal -> OYSTER_BAL[b]
    |
    when (b > 0) snd.oyster.gin -> OYSTER_BAL[b]
).
'''

def gen_run(n, mode, opt=True):
    rld_card = ','.join(['"rld.card.%d"' % i for i in range(1, n+1)])
    rld_oyster = ','.join(['"rld.oyster.%d"' % i for i in range(1, n+1)])
    return f'''
{{
  "sys": ["gate-in.lts", "gate-out.lts", "card-bal.lts", "oyster-bal.lts"],
  "env": [],
  "dev": ["human-dev.lts"],
  "safety": ["no-collision.lts"],
  "method": {'"supervisory"' if opt else '"supervisory-non-opt"'},
  "options": {{
    "progress": ["rcv.oyster.fin", "rcv.card.fin"],
    "preferredMap": {{
      "3": [
        ["snd.oyster", "snd.card.gin", "snd.oyster.gin", "rcv.oyster.fin"],
        ["snd.card", "snd.oyster.gin", "snd.card.gin", "rcv.card.fin"]
      ]
    }},
    "controllableMap": {{
      "0": [        
        "rcv.oyster.gin",
        "rcv.card.gin",

        "rcv.oyster.fin",
        "rcv.card.fin",

        {rld_card},
        {rld_oyster}
      ],
      "3": [
        "snd.oyster",
        "snd.card",
        
        "snd.oyster.gin",
        "snd.card.gin"
      ]
    }},
    "observableMap": {{
      "0": [
        "snd.oyster",
        "snd.card",
        "rcv.oyster.gin",
        "rcv.card.gin",

        "snd.oyster.gin",
        "snd.card.gin",
        "rcv.oyster.fin",
        "rcv.card.fin",

        "no_oyster_bal",
        {rld_card},
        {rld_oyster}
      ]
    }},
    "algorithm": "{mode}"
  }}
}}
'''

def gen_oasis_simple(n, mode):
    rld_card = ','.join(['"rld.card.%d"' % i for i in range(1, n+1)])
    rld_oyster = ','.join(['"rld.oyster.%d"' % i for i in range(1, n+1)])
    return f'''
{{
  "sys": ["gate-in.lts", "gate-out.lts", "card-bal.lts", "oyster-bal.lts"],
  "env": [],
  "dev": ["human-dev.lts"],
  "safety": ["no-collision.lts"],
  "method": "{mode}",
  "options": {{
    "progress": ["rcv.oyster.fin", "rcv.card.fin"],
    "preferred": [
      ["snd.oyster", "snd.card.gin", "snd.oyster.gin", "rcv.oyster.fin"],
      ["snd.card", "snd.oyster.gin", "snd.card.gin", "rcv.card.fin"]
    ],
    "controllable": [
      "snd.oyster",
      "snd.card",
      "rcv.oyster.gin",
      "rcv.card.gin",

      "snd.oyster.gin",
      "snd.card.gin",
      "rcv.oyster.fin",
      "rcv.card.fin",

      {rld_card},
      {rld_oyster}
    ],
    "observable": [
      "snd.oyster",
      "snd.card",
      "rcv.oyster.gin",
      "rcv.card.gin",

      "snd.oyster.gin",
      "snd.card.gin",
      "rcv.oyster.fin",
      "rcv.card.fin",

      "no_oyster_bal",
      {rld_card},
      {rld_oyster}
    ]
  }}
}}
'''

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: generate.py <n>")
        exit(1)
    n = int(sys.argv[1])

    with open("card-bal.lts", "w") as f:
        f.write(gen_card_bal(n))
    
    with open("oyster-bal.lts", "w") as f:
        f.write(gen_oyster_bal(n))
    
    with open("config-pareto.json", "w") as f:
        f.write(gen_run(n, "Pareto"))
    
    with open("config-pareto-non-opt.json", "w") as f:
        f.write(gen_run(n, "Pareto", opt=False))

    with open("config-fast.json", "w") as f:
        f.write(gen_run(n, "Fast"))
    
    with open("config-oasis.json", "w") as f:
        f.write(gen_oasis_simple(n, "oasis"))
    
    with open("config-simple.json", "w") as f:
        f.write(gen_oasis_simple(n, "simple"))
