import sys


def gen_sys(n, m):
  return f'''
range N_VOTER = 1..{n}

EM = (password -> P1),
P1 = (select -> P2),
P2 = (vote -> P3 | back -> P1),
P3 = (confirm -> EM | back -> P2).
'''


def gen_env(n, m):
  return f'''
range N_VOTER = 1..{n}
range N_EO = 1..{m}

ENV = (v[i:N_VOTER].enter -> VOTER[i] | eo[j:N_EO].enter -> EO[j]),
VOTER[i:N_VOTER] = (password -> VOTER1[i]),
VOTER1[i:N_VOTER] = (select -> VOTER2[i]),
VOTER2[i:N_VOTER] = (vote -> VOTER3[i] | back -> VOTER1[i]),
VOTER3[i:N_VOTER] = (confirm -> v[i].exit -> ENV | back -> VOTER2[i]),
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

def gen_dev(n, m):
  return f'''
range N_VOTER = 1..{n}
range N_EO = 1..{m}

ENV = (v[i:N_VOTER].enter -> VOTER[i] | eo[j:N_EO].enter -> EO[j]),
VOTER[i:N_VOTER] = (password -> VOTER1[i]),
VOTER1[i:N_VOTER] = (select -> VOTER2[i]),
VOTER2[i:N_VOTER] = (vote -> VOTER3[i] | back -> VOTER1[i]),
VOTER3[i:N_VOTER] = (confirm -> v[i].exit -> ENV | omit_confirm -> v[i].exit -> ENV | back -> VOTER2[i]),
EO[j:N_EO] = (select -> EO[j] | vote -> EO[j] | confirm -> eo[j].exit -> ENV | back -> EO[j] | eo[j].exit -> ENV).
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
  
  with open("dev.lts", "w") as f:
    f.write(gen_dev(n, m))
