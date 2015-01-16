(
  SELECT
    'MSI'               AS type,
    m.id                AS id,
    m.dateTimeUpdated   AS updated,
    m.validFrom         AS validFrom,
    m.validTo           AS validTo,
    a.sortOrder         AS area_sort_order,
    m.sortOrder         AS msg_sort_order
  FROM
    message m

--  The MSI admin use these criteria (where uc.abbreviation = 'DK') ... but we do not want access to the user table
--    LEFT JOIN user u ON m.usernameId = u.id
--    LEFT JOIN organisation o ON o.id = u.organisationId
--    LEFT JOIN country uc ON uc.id = o.countryId

    LEFT JOIN location loc ON m.locationId = loc.id
    LEFT JOIN locationtype loctp ON loc.locationTypeId = loctp.id
    LEFT JOIN main_area a ON loc.areaId = a.id
    LEFT JOIN country c ON a.countryId = c.id
  WHERE
    c.abbreviation = 'DK'
    AND m.validFrom < now() + INTERVAL 1 DAY
    AND (m.validTo IS NULL OR m.validTo > now())
    AND m.isLatest = 1
    AND m.dateTimeDeleted IS NULL
    AND draft = 0
)
UNION
(
  SELECT
    'FE'              AS type,
    fp.id             AS id,
    fp.creation_time  AS updated,
    fp.t_from         AS valid_from,
    fp.t_to           AS valid_to,
    ma.sortOrder      AS area_sort_order,
    0                 AS msg_sort_order
  FROM
    firing_period fp
    LEFT JOIN firing_area fa ON fp.f_area_id = fa.id
    LEFT JOIN main_area ma   ON fa.main_area_id = ma.id
    LEFT JOIN country c      ON ma.countryId = c.id
  WHERE
    date(fp.t_from) >= CURRENT_DATE
    AND date(fp.t_from) <= CURRENT_DATE + 1
    AND fp.t_to > CURRENT_TIMESTAMP
)
ORDER BY area_sort_order, msg_sort_order
