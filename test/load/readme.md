
In one terminal, start the server with a clean slate:
./setgo.sh

In another terminal, upload generated CVRs for the first 3 counties:
./genrun.sh import -c 3

Define the audit and audit Round 1
./genrun.sh init
./genrun.sh startRound


Mimic the audit boards polling the dashboard, 3 per county(30second / 3 = 10
second pauses (-p)):

cat admins.txt | xargs -n 1 -P 64 ./polling.sh -p 10 -n 10

