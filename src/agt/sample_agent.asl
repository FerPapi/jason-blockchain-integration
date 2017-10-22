// Agent sample_agent in project example_blockchain

/* Initial beliefs and rules */

/* Initial goals */
msg("Whatevs").
!start.

/* Plans */

+!start : msg(M) <- getNode(M).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization
//{ include("$jacamoJar/templates/org-obedient.asl") }
