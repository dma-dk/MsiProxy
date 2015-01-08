SELECT
  m.id as id,
  m.dateTimeUpdated as updated
FROM
  message m
LEFT JOIN user u on m.usernameId = u.id
LEFT JOIN organisation o on o.id = u.organisationId
LEFT JOIN country c on c.id = o.countryId
WHERE
  c.abbreviation = 'DK'
  and  m.validFrom < now() + interval 1 day
  AND (m.validTo IS NULL OR m.validTo > now())
  AND m.isLatest=1
  AND m.dateTimeDeleted IS NULL
  and draft=0
ORDER BY m.sortOrder
