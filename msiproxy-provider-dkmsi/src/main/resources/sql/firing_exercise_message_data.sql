SELECT
  fp.creation_time  AS created,
  fp.creation_time  AS updated,
  fp.t_from         AS valid_from,
  fp.t_to           AS valid_to,
  c.id              AS area1_id,
  c.country_english AS area1_en,
  c.country_danish  AS area1_da,
  ma.id + 1000      AS area2_id,
  ma.area_english   AS area2_en,
  ma.area_danish    AS area2_da,
  fa.id + 2000      AS area3_id,
  fa.name_eng       AS area3_en,
  fa.name_dk        AS area3_da,
  i.description_eng AS description_en,
  i.description_dk  AS description_da,
  i.info_type_id    AS info_type
FROM
  firing_period fp
  LEFT JOIN firing_area fa ON fp.f_area_id = fa.id
  LEFT JOIN main_area ma ON fa.main_area_id = ma.id
  LEFT JOIN country c ON ma.countryId = c.id
  LEFT JOIN firing_area_information fai ON fai.firing_area_id = fa.id
  LEFT JOIN information i ON i.id = fai.information_id
WHERE
  fp.id = :id
ORDER BY
  i.info_type_id;
