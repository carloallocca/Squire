# Test and evaluation data for SQUIRE

This directory does not just contain the data for unit-testing the SQUIRE application: it also contains the data used for evaluating the approach.

In particular, the file `goldstandard.json` contains some groups of SPARQL endpoint pairs in an application domain. For each pair, there is a group of items with the following attributes:

- `original` : a SPARQL domain query satisfiable by the `source` endpoint (i.e. the result set at the time of this commit is non-empty)
- `expected` : one or more SPARQL domain queries satisfiable by the `target` endpoint, which are the closest semantic match to the original query

Given a set of fixed weights, this gold standard can be used to evaluate:

1. Whether the expected query is returned by SQUIRE.
2. How long it takes for that particular query to appear in the list of recommendations.
3. Where that query appears in the ranked list, both at the moment it appears, and at the end of the entire computation.