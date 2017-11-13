-- For each county and each batch, compare batch counts reflected in manifest vs CVR file.

SELECT bmi.county_id,bmi.scanner_id,bmi.batch_id, bmi.batch_size, count(cvr.id) as count_cvr, (bmi.batch_size -count(cvr.id)) as discrepancy from ballot_manifest_info AS bmi
    LEFT JOIN cast_vote_record AS cvr
        ON bmi.county_id = cvr.county_id 
            AND bmi.batch_id = cvr.batch_id
            AND bmi.scanner_id = cvr.scanner_id
WHERE cvr.record_type='UPLOADED' 
GROUP BY bmi.county_id, bmi.scanner_id, bmi.batch_id, bmi.batch_size
ORDER BY bmi.county_id, bmi.scanner_id, bmi.batch_id, bmi.batch_size
;