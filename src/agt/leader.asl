// leader agent

{ include("$jacamoJar/templates/common-cartago.asl") }

/*
 * By Joao Leite
 * Based on implementation developed by Rafael Bordini, Jomi Hubner and Maicon Zatelli
 */
 /* new initial belief */

winning(none,0).


score(miner1,0).
score(miner2,0).
score(miner3,0).
score(miner4,0).

//the start goal only works after execise j)
//!start.
//+!start <- tweet("a new mining is starting! (posted by jason agent)").
/* new plans for event +dropped */

/* new plans for event +dropped */

+dropped[source(A)] : score(A,S) & winning(L,SL) & S+1>SL
   <- -score(A,S);
      +score(A,S+1);
      -dropped[source(A)];
      -+winning(A,S+1);
      .print("Agent ",A," is winning with ",S+1," pieces of gold");
      .broadcast(tell,winning(A,S+1)).

+dropped[source(A)] : score(A,S)
   <- -score(A,S);
      +score(A,S+1);
      -dropped[source(A)];
      .print("Agent ",A," has dropped ",S+1," pieces of gold").
