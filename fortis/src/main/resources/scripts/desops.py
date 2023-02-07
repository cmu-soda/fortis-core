import socket
from typing import Optional

import sys
import ast
import igraph as ig
import DESops as d

from DESops import error
from DESops.automata.DFA import DFA
from DESops.automata.event import Event
from DESops.automata.NFA import NFA
from DESops.automata.PFA import PFA
from pydash import flatten_deep


def read_nonempty_line(f) -> str:
    while True:
        line = f.readline()
        # EOF
        if line == "":
            raise error.FileFormatError("Reach unexpected EOF")
        line = line.strip()
        if line == "":
            continue
        return line


def read_states(f, g: ig.Graph, num_states: int) -> Optional[ig.Graph]:
    g_defined = False
    type_aut = ""

    state_markings = list()
    state_names = list()
    state_crit = list()
    events = set()
    events_unobs = set()
    events_unctr = set()
    trans_list = list()
    trans_labels = list()
    trans_observable = list()
    trans_controllable = list()
    trans_prob = list()
    neighbors_list = list()

    for _ in range(num_states):
        line = read_nonempty_line(f)
        # Should be delimited in the line by tabs
        states_tuple = line.split("\t")
        if len(states_tuple) < 3:
            raise error.FileFormatError(
                f"ERROR: Missing argument at '{line}'. States are in the format: SOURCE_STATE\tMARKED\t#TRANSITIONS"
            )
        name = states_tuple[0]
        states_tuple[0] = ast.literal_eval(name) if name[0] == "(" else name
        state_names.append(states_tuple[0])
        state_markings.append(states_tuple[1] == "1")
        if len(states_tuple) > 3:
            state_crit.append(states_tuple[2])
        total = 0
        neigh = list()

        # set for each vertex to see if any nondeterminism, i.e. repeated labels
        # if any found, set type to NFA, and future vertices won't do this check
        unique_labels = set()
        try:
            num_trans = int(states_tuple[2])
        except ValueError:
            raise error.FileFormatError(
                f"ERROR: Expected integer number of neighbors at '{line}'. Did previous state have more transitions than noted?"
            )

        for _ in range(0, num_trans):
            line = read_nonempty_line(f)
            trans_tuple = line.split("\t")
            # TODO: Not sure why need this
            # if trans_tuple == ["\n"]:
            #     raise error.FileFormatError(
            #         "ERROR %s:\nToo many transitions at state %s"
            #         % (fsm_filename, states_tuple[0])
            #     )
            if len(trans_tuple) > 5 or len(trans_tuple) < 4:
                raise error.FileFormatError(
                    f"ERROR: Wrong arguments at '{line}'. Transitions are in the format: EVENT\tTARGET_STATE\tc/uc\to/uo\tprob(optional)"
                )
            if not g_defined and not type_aut:
                if len(trans_tuple) == 5:
                    type_aut = "PFA"
                elif len(trans_tuple) == 4:
                    # TODO WHEN NFA IS DEFINED THEN SET AS DFA UNTIL A NONDETERMINISTIC TRANS IS FOUND
                    type_aut = "DFA"
            if type_aut == "PFA" and len(trans_tuple) != 5:
                raise error.FileFormatError(
                    f"ERROR: line '{line}' PFA transitions are in the format: EVENT\tTARGET_STATE\tc/uc\to/uo\tprob"
                )
            elif (type_aut == "DFA" or type_aut == "NFA") and len(trans_tuple) != 4:
                raise error.FileFormatError(
                    f"ERROR: line '{line}' DFA transitions are in the format:\nEVENT\tTARGET_STATE\tc/uc\to/uo"
                )

            trans_labels.append(Event(trans_tuple[0]))
            events.add(Event(trans_tuple[0]))
            t_name = trans_tuple[1]
            trans_tuple[1] = (ast.literal_eval(t_name) if t_name[0] == "(" else t_name)
            trans_list.append((states_tuple[0], trans_tuple[1]))
            if trans_tuple[2] == "uc":
                events_unctr.add(Event(trans_tuple[0]))
            trans_controllable.append(trans_tuple[2])
            if trans_tuple[3] == "uo":
                events_unobs.add(Event(trans_tuple[0]))
            trans_observable.append(trans_tuple[3])

            if type_aut == "PFA":
                # probabilistic info encoded must be a PFA
                type_aut = "PFA"
                try:
                    if float(trans_tuple[4]) > 1 or float(trans_tuple[4]) < 0:
                        raise ValueError
                except ValueError:
                    raise error.FileFormatError(
                        "ERROR: Probability value must be a number smaller than or equal to 1"
                    )
                trans_prob.append(trans_tuple[4])
                total = total + float(trans_tuple[4])

                # PFA out attr is of form (target, event, prob)
                neigh.append(
                    (trans_tuple[1], Event(trans_tuple[0]), float(trans_tuple[4]))
                )
            else:
                neigh.append((trans_tuple[1], Event(trans_tuple[0])))
                if type_aut == "DFA" and trans_tuple[0] in unique_labels:
                    type_aut = "NFA"
                elif type_aut != "NFA":
                    unique_labels.add(trans_tuple[0])

        neighbors_list.append(neigh)
        if total > 0 and total != 1:
            raise error.FileFormatError(
                "ERROR: Transitions in state %s do not sum up to 1"
                % (states_tuple[0])
            )

    # Construct graph
    g.vs["marked"] = state_markings
    if state_crit:
        g.vs["crit"] = state_crit
    g.vs["name"] = state_names
    trans_list_int_names = list()
    for pair in trans_list:
        source = state_names.index(pair[0])
        target = state_names.index(pair[1])
        trans_list_int_names.append((source, target))

    if g_defined:
        g.Euc.update(events_unctr)
        g.Euo.update(events_unobs)
        g.events = events.copy()

    if type_aut == "DFA" or isinstance(g, DFA):
        if not g_defined:
            g = DFA(g, events_unctr, events_unobs, events, False)
        g.add_edges(trans_list_int_names, trans_labels, fill_out=False)

    elif type_aut == "PFA" or isinstance(g, PFA):
        if not g_defined:
            g = PFA(g, events_unctr, events_unobs, events)
        g.add_edges(trans_list_int_names, trans_labels, trans_prob, fill_out=False)

    elif type_aut == "NFA" or isinstance(g, NFA):
        if not g_defined:
            g = NFA(g, events_unctr, events_unobs, events)
        g.add_edges(trans_list_int_names, trans_labels, fill_out=False)

    trans_observable_bool = [x == "o" for x in trans_observable]
    g.es["obs"] = trans_observable_bool
    trans_controllable_bool = [x == "c" for x in trans_controllable]
    g.es["contr"] = trans_controllable_bool

    if type_aut == "PFA":
        neighbors_list = [
            [g.Out(state_names.index(adj[0]), adj[1], adj[2]) for adj in l]
            for l in neighbors_list
        ]
    else:
        neighbors_list = [
            [g.Out(state_names.index(adj[0]), adj[1]) for adj in l]
            for l in neighbors_list
        ]

    g.vs["out"] = neighbors_list

    return g


def read_fsm(f) -> Optional[ig.Graph]:
    try:
        line = read_nonempty_line(f)
        num_states = int(line)
    except ValueError:
        sys.stderr.write("ERROR: Need the number of states\n")
        return None

    g = ig.Graph(directed=True)
    g.add_vertices(num_states)

    try:
        return read_states(f, g, num_states)
    except error.FileFormatError as e:
        sys.stderr.write(repr(e) + "\n")
        return None


def write_fsm(f, g, plot_prob=False, flatten_state_name=False):
    # If obs/contr attributes are not defined, mark them as true
    if "obs" not in g.es.attributes():
        if not g.Euo:
            g.es["obs"] = [True]
    if "contr" not in g.es.attributes():
        if not g.Euc:
            g.es["contr"] = [True]

    not_marked = False
    if "marked" not in g.vs.attributes():
        not_marked = True

    if "name" not in g.vs.attributes():
        g.vs["name"] = list(range(0, g.vcount()))
    elif flatten_state_name is True:
        g.vs["name"] = [",".join(flatten_deep(v["name"])) for v in g.vs]

    f.write(str(g.vcount()))
    f.write("\n\n")

    for v in g.vs:
        # print(','.join(v["name"]))
        t = v["name"]
        f.write(str2(v["name"]))
        f.write("\t")
        if not_marked:
            f.write("0")
        else:
            t = v["marked"]
            f.write("1" if t else "0")
        f.write("\t")

        edge_seq = g.es.select(_source=v.index)
        f.write(str(len(edge_seq)))
        f.write("\n")
        for trans in edge_seq:
            if isinstance(trans["label"], Event):
                f.write(trans["label"].label)
            else:
                f.write(str2(trans["label"]))
            f.write("\t")
            f.write(str2(g.vs["name"][trans.target]))
            f.write("\t")
            if g.Euc:
                f.write("c" if trans["label"] not in g.Euc else "uc")
            else:
                f.write("c" if trans["contr"] else "uc")
            f.write("\t")
            if g.Euo:
                f.write("o" if trans["label"] not in g.Euo else "uo")
            else:
                f.write("o" if trans["obs"] else "uo")
            if plot_prob and "prob" in g.es.attributes():
                f.write("\t")
                f.write(trans["prob"])
            f.write("\n")
        f.write("\n")


def str2(name):
    """
    Smarter str casting for frozensets. Converting
    frozenset to set makes 'frozenset({1,2,3})'
    as 'set({1,2,3})' which prints as '{1,2,3}'.
    """
    if isinstance(name, frozenset):
        return str(set(name))
    return str(name)


if __name__ == "__main__":
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind(("127.0.0.1", 5000))
        s.listen()
        while True:
            conn, _ = s.accept()
            with conn.makefile("rw") as f:
                plant = read_fsm(f)
                prop = read_fsm(f)

                if plant is not None and prop is not None:
                    L = d.supervisor.offline_VLPPO(plant, prop)
                    L.vs["marked"] = [1 for i in range(L.vcount())]
                    L = d.composition.parallel(L, plant, prop)
                    L = d.supervisor.supremal_sublanguage(plant, L, prefix_closed=False, mode=d.supervisor.Mode.CONTROLLABLE_NORMAL)
                    # L_observed = d.composition.observer(L)

                    if len(L.vs) != 0:
                        f.write("0\n")
                        write_fsm(f, L)
                    else:
                        f.write("1\n")
                else:
                    f.write("2\n")
                
                f.flush()
            conn.close()
