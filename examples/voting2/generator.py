import sys


def gen_sys(n, m):
  return f'''
range N_VOTER = 1..{n}

EM = (password -> P1),
P1 = (select -> P2),
P2 = (vote -> P3 | back -> P1),
P3 = (confirm -> v[N_VOTER].done -> EM | back -> P2).
'''


def gen_env(n, m):
  return f'''
range N_VOTER = 1..{n}
range N_EO = 1..{m}

ENV = (v[i:N_VOTER].enter -> VOTER[i] | eo[j:N_EO].enter -> EO[j]),
VOTER[i:N_VOTER] = (password -> VOTER[i] | select -> VOTER[i] | vote -> VOTER[i] | confirm -> v[i].done -> v[i].exit -> ENV | back -> VOTER[i] | v[i].exit -> ENV),
EO[j:N_EO] = (select -> EO[j] | vote -> EO[j] | confirm -> eo[j].exit -> ENV | back -> EO[j] | eo[j].exit -> ENV).
'''


def gen_p(n, m):
  return f'''
const NoBody = 0
const VOTERS = {n}

range N_VOTER = 1..{n}
range N_EO = 1..{m}

range WHO = NoBody..{m+n}

P = VOTE[NoBody][NoBody][NoBody],
VOTE[in:WHO][sel:WHO][v:WHO] = (
      v[i:N_VOTER].enter -> VOTE[i][sel][v] | eo[j:N_EO].enter -> VOTE[VOTERS+j][sel][v]
    | password -> VOTE[in][sel][in]
    | select -> VOTE[in][in][v]
    | when (in > 0 && in <= VOTERS && in == sel && sel == v) confirm -> VOTE[in][NoBody][NoBody]
    | when (in > VOTERS && sel == v) confirm -> VOTE[in][NoBody][NoBody]
).
'''


def gen_run(n, m, mode, opt=True):
  enter_exits = ", ".join(
    [f'"v.{i}.enter"' for i in range(1, n+1)] +
    [f'"v.{i}.exit"' for i in range(1, n+1)] +
    [f'"eo.{j}.enter"' for j in range(1, m+1)] +
    [f'"eo.{j}.exit"' for j in range(1, m+1)]
  )
  dones = ", ".join([f'"v.{i}.done"' for i in range(1, n+1)])
  return f'''
{{
  "sys": ["sys.lts"],
  "env": [""],
  "dev": ["env.lts"],
  "safety": ["p.lts"],
  "method": {'"supervisory"' if opt else '"supervisory-non-opt"'},
  "options": {{
    "progress": [{dones}],
    "preferredMap": {{
      "3": [ ["select", "back", "select", "vote", "confirm"] ]
    }},
    "controllableMap": {{
      "1": ["back", "confirm", "password", "select", "vote", {dones}],
      "3": [{enter_exits}]
    }},
    "observableMap": {{
      "0": ["back", "confirm", "password", "select", "vote", {dones}],
      "2": [{enter_exits}]
    }},
    "algorithm": "{mode}"
  }}
}}
'''


def gen_oasis(n, m):
  dones = ", ".join([f'"v.{i}.done"' for i in range(1, n+1)])
  return f'''
{{
  "sys": ["sys.lts"],
  "env": [""],
  "dev": ["env.lts"],
  "safety": ["p.lts"],
  "method": "oasis",
  "options": {{
    "progress": [{dones}],
    "preferred": [ ["select", "back", "select", "vote", "confirm"] ]
  }}
}}
'''


def gen_simple(n, m):
  dones = ", ".join([f'"v.{i}.done"' for i in range(1, n+1)])
  return f'''
{{
  "sys": ["sys.lts"],
  "env": [""],
  "dev": ["env.lts"],
  "safety": ["p.lts"],
  "method": "simple",
  "options": {{
    "progress": [{dones}],
    "preferred": [ ["select", "back", "select", "vote", "confirm"] ]
  }}
}}
'''


if __name__ == "__main__":
  if len(sys.argv) < 3:
    print("Usage: generator.py <num of voters> <num of officials>")
    exit(0)
  n, m = int(sys.argv[1]), int(sys.argv[2])

  with open("sys.lts", "w") as f:
    f.write(gen_sys(n, m))
  
  with open("env.lts", "w") as f:
    f.write(gen_env(n, m))
  
  with open("p.lts", "w") as f:
    f.write(gen_p(n, m))
  
  with open("config-pareto.json", "w") as f:
    f.write(gen_run(n, m, "Pareto"))
  
  with open("config-pareto-non-opt.json", "w") as f:
    f.write(gen_run(n, m, "Pareto", opt=False))
  
  with open("config-fast.json", "w") as f:
    f.write(gen_run(n, m, "Fast"))
  
  with open("config-oasis.json", "w") as f:
    f.write(gen_oasis(n, m))
  
  with open("config-simple.json", "w") as f:
    f.write(gen_simple(n, m))
