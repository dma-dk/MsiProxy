SELECT
  fap.lat_deg       as lat_deg,
  fap.lat_min       as lat_min,
  fap.long_deg      as lon_deg,
  fap.long_min      as lon_min
FROM
  firing_period fp
  LEFT JOIN firing_area fa ON fp.f_area_id = fa.id
  LEFT JOIN firing_area_position fap ON fap.firing_area_id = fa.id
WHERE
  fp.id = :id
ORDER BY 
  fap.sort_order;
