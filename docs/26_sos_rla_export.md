% Secretary of State Manual
% Colorado Risk Limiting Audit Tool Data Export Tool
% 2017


## Exporting Data

### Minimum Data Required to Allow Public to Reproduce Audit Calculations

#### m_audit_details_by_contest_and_ballot.sql

 For each contest under audit, and for each cast vote record presented to the 
  Audit Board for verification, 
  the RLA system's record of the Audit Board's review of 
  the physical ballot for that contest
  

 Column Name | Type | _____________Meaning_____________
--- | --- | ---
county_name | Text String | Name of County
contest_name | Text String | Name of contest  
random_sequence_index | Integer | Index in the random sequence (starting with 1)
 imprinted_id | Text String | combination of scanner, batch and record ids  that uniquely identifies the ballot card  and may be imprinted on the card
 ballot_type | Text String | BallotType from Dominion CVR export file, a code for the set of contests that  should be present on the physical ballot card
 choice_per_voting_computer | List of Text Strings | List of voter choices in the given contest on the given ballot card, as interpreted by the vote-tabulation computer system (note: overvotes recorded as blank votes)
 choice_per_audit_board | List of Text Strings | List of voter choices in the given contest on the given ballot card, as interpreted by the Audit Board (note: overvotes recorded as a too-long list of choices)
 did_audit_board_agree | Yes/No | "Yes" if the Audit Board came to consensus on the interpretation of the given ballot card; "No" if not;  blank if the card has not been reviewed by the Audit Board.
 audit_board_comment | Text String | Text of comment entered by Audit Board  about the given contest on the given ballot card, or indication that the ballot was not found.
 timestamp | Timestamp | Date and time of Audit Board's submission of their interpretation to the RLA Tool


#### m_ballot_list_for_review.sql

List of ballot cards assigned to Audit Board for review. 
  (This list could be created from the random sequence by removing duplicates 
  and ordering by tabulator, batch and position within the batch.) 
  Within each county, the list is ordered by rounds 
  and, within each round, by tabulator, batch and position within the batch.


 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
 round | Integer | The audit round number in which the ballot card is assigned  to the given County's Audit Board for review.
 scanner_id | Integer | TabulatorNum from Dominion CVR export file,  identifying the tabulator used to read the physical ballot card   
 batch_id | Integer | BatchId from Dominion CVR export file,  identifying the batch of physical ballot cards in which the card was scanned
 record_id | Integer | RecordId from Dominion CVR export file, indicating the position of the card  in its batch of physical ballot cards 
 imprinted_id | Text String | combination of scanner, batch and record ids  that uniquely identifies the ballot card  and may be imprinted on the card
 ballot_type | Text String | BallotType from Dominion CVR export file, a code for the set of contests that  should be present on the physical ballot card
  
#### m_selected_contest_static.sql

List of contests selected by the Secretary of State for audit, with information 
  about the contest that doesn't change during the audit, namely the reason for 
  the audit, the number of winners allowed in the contest, the tabulated winners of the contest, the numbers of ballots cards recorded as cast in the county (total number as well as the number containing the given contest) and the value of the error inflation factor (gamma).


 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
contest_name | Text String | Name of contest 
contest_type | Text String | Type of contest (statewide or countywide, per Rule 25.2.2(i))
 winners_allowed | Integer | Number of winners allowed for the contest (required to calculate diluted margin)
winners | List of Text Strings | List of all winners of the given contest in the given County. (Note that for multi-county contests this list includes the highest vote-getters within the County, even if these were not the winners across all Counties.)
min_margin | Integer | The smallest margin between any winner and any loser
county_ballot_card_count | Integer | The number of ballot cards recorded in the given County in the election (including cards that do not contain the contest in question)
contest_ballot_card_count | Integer | The number of ballot cards recorded in the given County that contain the contest in question
 gamma | Number | Error inflation factor defined in Stark's paper, Super-simple simultaneous single-ballot risk-limiting audits, which is cited in Lindeman and Stark's paper, A Gentle Introduction to Risk Limiting Audits, which is cited in Rule 25.2.2(j))

  
#### m_selected_contest_dynamic.sql

 List of contests selected by Secretary of State for audit, with current status. 
  Which contests has the 
  Secretary selected for audit? Which contests (if any) has the 
  Secretary selected for hand count? How many discrepancies of each type?

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
contest_name | Text String | Name of contest 
current_audit_type | Text String | Comparison audit, ballot polling audit or hand count
 computerized_audit_status | Text String | Not started, in progress, risk limit achieved, or ended.  Because declaring a hand count ends the computerized portion of the audit, a contest that is being hand-counted will have the value "ended" in this field.
 one_vote_over_count | Integer | The number of ballot cards in the random sequence so far (with duplicates)  on which there is a one-vote overstatement  (per Lindeman & Stark's A Gentle Introduction to Risk Limiting Audits).
 one_vote_under_count | Integer | The number of ballot cards in the random sequence so far (with duplicates)  on which there is a one-vote understatement  (per Lindeman & Stark's A Gentle Introduction to Risk Limiting Audits).
 two_vote_over_count | Integer | The number of ballot cards in the random sequence so far (with duplicates)  on which there is a two-vote overstatement  (per Lindeman & Stark's A Gentle Introduction to Risk Limiting Audits).
 two_vote_under_count | Integer | The number of ballot cards in the random sequence so far (with duplicates)  on which there is a two-vote understatement  (per Lindeman & Stark's A Gentle Introduction to Risk Limiting Audits).


#### m_cvr_hash.sql

Hashes of CVR files

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
 hash | Text String | Hash value entered by the given county after uploading the cast vote record file to be used in the audit

#### m_manifest_hash.sql

Hashes of ballot manifest files

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
 hash | Text String | Hash value entered by the given county after uploading the ballot manifest file
     to be used in the audit



  
#### m_random_sequence.sql

Random sequence of ballot cards used for the audit. 
  (This random sequence is generated ?with replacement? and thus may include duplicates.)
  
 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
 review_index | Integer | The position in the random sequence for the given County
 scanner_id | Integer | TabulatorNum from Dominion CVR export file,  identifying the tabulator used to read the physical ballot card    with the given review-index
 batch_id | Integer | BatchId from Dominion CVR export file,  identifying the batch of physical ballot cards in which the card with the given review-index was scanned
 record_id | Integer | RecordId from Dominion CVR export file, indicating the position of the card  with the given review-index in its batch of physical ballot cards 
 imprinted_id | Text String | combination of scanner, batch and record ids  that uniquely identifies the ballot card  with the given review-index and may be imprinted on the card
 ballot_type | Text String | BallotType from Dominion CVR export file, a code for the set of contests that  should be present on the physical ballot card with the given review-index

#### m_tabulate.sql

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
contest_name | Text String | Name of contest 
choice | Text String | Name of candidate or Yes or No for a ballot question
votes | Integer | Number of votes recorded for the given choice in the given contest in the given County
votes_allowed | Integer | Maximum number of choices that can be recorded in the given contest on a single ballot


### Other Data Exports 

#### all_contest_static.sql

List of all contests, with information about the contest that doesn't change during the audit, namely the reason for the audit, the number of winners allowed in the contest, the tabulated winners of the contest, the numbers of ballots cards recorded as cast in the county (total number as well as the number containing the given contest) and the value of the error inflation factor (gamma).

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
contest_name | Text String | Name of contest 
contest_type | Text String | Type of contest (statewide or countywide, per Rule 25.2.2(i))
 winners_allowed | Integer | Number of winners allowed for the contest (required to calculate diluted margin)
winners | List of Text Strings | List of all winners of the given contest in the given County. (Note that for multi-county contests this list includes the highest vote-getters within the County, even if these were not the winners across all Counties.)
min_margin | Integer | The smallest margin between any winner and any loser
county_ballot_card_count | Integer | The number of ballot cards recorded in the given County in the election (including cards that do not contain the contest in question)
contest_ballot_card_count | Integer | The number of ballot cards recorded in the given County that contain the contest in question
 gamma | Number | Error inflation factor defined in Stark's paper, Super-simple simultaneous single-ballot risk-limiting audits, which is cited in Lindeman and Stark's paper, A Gentle Introduction to Risk Limiting Audits, which is cited in Rule 25.2.2(j))

#### all_contest_dynamic.sql

List of contests with current status.  Which contests has the  Secretary selected for audit? Which contests (if any) has the  Secretary selected for hand count? How many discrepancies of each type have been found so far?

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
contest_name | Text String | Name of contest 
current_audit_type | Text String | Comparison audit, ballot polling audit or hand count
 computerized_audit_status | Text String | Not started, in progress, risk limit achieved, or ended.  Because declaring a hand count ends the computerized portion of the audit, a contest that is being hand-counted will have the value "ended" in this field.
 one_vote_over_count | Integer | The number of ballot cards in the random sequence so far (with duplicates)  on which there is a one-vote overstatement  (per Lindeman & Stark's A Gentle Introduction to Risk Limiting Audits).
 one_vote_under_count | Integer | The number of ballot cards in the random sequence so far (with duplicates)  on which there is a one-vote understatement  (per Lindeman & Stark's A Gentle Introduction to Risk Limiting Audits).
 two_vote_over_count | Integer | The number of ballot cards in the random sequence so far (with duplicates)  on which there is a two-vote overstatement  (per Lindeman & Stark's A Gentle Introduction to Risk Limiting Audits).
 two_vote_under_count | Integer | The number of ballot cards in the random sequence so far (with duplicates)  on which there is a two-vote understatement  (per Lindeman & Stark's A Gentle Introduction to Risk Limiting Audits).



#### auditboards.sql

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
member  | Text String | Name of audit board member
sign_in_time | Timestamp |  Beginning of an audit board member's RLA Tool session
sign_out_time  | Timestamp |  End of the given session for the given audit board member

#### prefix_length.sql

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
audited_prefix_length | Integer | Length of the prefix of the random sequence of ballot card selections that will be audited by the end of the current round

#### seed.sql

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
seed | 20-Digit String | the random seed for the pseudo-random number generator

#### upload_status.sql

 Column Name | Type | _____________Meaning_____________ 
--- | --- | ---
county_name | Text String | Name of County
filename | Text String | Name of file
hash_status | Text String | "VERIFIED" or "MISMATCH"
approx_count | Integer | 
size | Integer |
status | "IMPORTED" or "NOT_IMPORTED"
timestamp | Timestamp | 

### State and County Audit Reports

Other export files are the same as the files available via the GUI interface,
for example ``state_report.xlsx``.

