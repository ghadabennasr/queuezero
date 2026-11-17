-- V2: demo categories, fictional locations, opening hours, simulation profiles

INSERT INTO service_categories (code, name, icon) VALUES
 ('POST',         'Post office',              'mail'),
 ('MUNICIPALITY', 'Municipal office',         'account_balance'),
 ('HOSPITAL',     'Public hospital / clinic', 'local_hospital'),
 ('TAX',          'Tax office',               'receipt_long');

INSERT INTO service_locations (category_id, name, address, governorate, latitude, longitude) VALUES
 ((SELECT id FROM service_categories WHERE code='POST'),         'Demo Post Office - Tunis Centre',     'Fictional address, Tunis',    'Tunis',    36.8065, 10.1815),
 ((SELECT id FROM service_categories WHERE code='POST'),         'Demo Post Office - Sousse Medina',    'Fictional address, Sousse',   'Sousse',   35.8256, 10.6084),
 ((SELECT id FROM service_categories WHERE code='MUNICIPALITY'), 'Demo Municipality - Ariana',          'Fictional address, Ariana',   'Ariana',   36.8665, 10.1647),
 ((SELECT id FROM service_categories WHERE code='MUNICIPALITY'), 'Demo Municipality - Monastir',        'Fictional address, Monastir', 'Monastir', 35.7643, 10.8113),
 ((SELECT id FROM service_categories WHERE code='HOSPITAL'),     'Demo Public Clinic - Sousse',         'Fictional address, Sousse',   'Sousse',   35.8300, 10.6380),
 ((SELECT id FROM service_categories WHERE code='HOSPITAL'),     'Demo Public Hospital - Sfax',         'Fictional address, Sfax',     'Sfax',     34.7406, 10.7603),
 ((SELECT id FROM service_categories WHERE code='TAX'),          'Demo Tax Office - Tunis',             'Fictional address, Tunis',    'Tunis',    36.8190, 10.1658),
 ((SELECT id FROM service_categories WHERE code='TAX'),          'Demo Tax Office - Bizerte',           'Fictional address, Bizerte',  'Bizerte',  37.2744,  9.8739);

-- Offices: Mon-Fri 08:00-17:00, Sat 08:00-12:00, Sun closed (weekday 0 = Monday)
INSERT INTO opening_hours (location_id, weekday, open_time, close_time, is_closed)
SELECT l.id, d.day,
       CASE WHEN d.day = 6 THEN NULL ELSE TIME '08:00' END,
       CASE WHEN d.day = 6 THEN NULL WHEN d.day = 5 THEN TIME '12:00' ELSE TIME '17:00' END,
       d.day = 6
FROM service_locations l
JOIN service_categories c ON c.id = l.category_id
CROSS JOIN generate_series(0, 6) AS d(day)
WHERE c.code <> 'HOSPITAL';

-- Hospitals / clinics: every day 07:00-21:00
INSERT INTO opening_hours (location_id, weekday, open_time, close_time, is_closed)
SELECT l.id, d.day, TIME '07:00', TIME '21:00', false
FROM service_locations l
JOIN service_categories c ON c.id = l.category_id
CROSS JOIN generate_series(0, 6) AS d(day)
WHERE c.code = 'HOSPITAL';

-- Simulation profiles for offices
INSERT INTO simulation_profiles
 (location_id, active_counters, avg_service_minutes, base_arrivals_per_hour, weekday_factors, hourly_factors)
SELECT l.id, p.counters, p.service_min, p.arrivals,
       ARRAY[1.3, 1.0, 0.9, 0.9, 1.1, 0.4, 0.0]::real[],
       ARRAY[0,0,0,0,0,0,0,0, 0.6,1.0,1.4,1.3,0.8,0.7,1.1,0.9,0.6,0.2, 0,0,0,0,0,0]::real[]
FROM (VALUES
  ('Demo Post Office - Tunis Centre',  3,  4.5, 25.0),
  ('Demo Post Office - Sousse Medina', 2,  5.0, 18.0),
  ('Demo Municipality - Ariana',       2,  8.0, 12.0),
  ('Demo Municipality - Monastir',     2,  7.0,  9.0),
  ('Demo Tax Office - Tunis',          3, 10.0, 12.0),
  ('Demo Tax Office - Bizerte',        2,  9.0,  6.0)
) AS p(name, counters, service_min, arrivals)
JOIN service_locations l ON l.name = p.name;

-- Simulation profiles for hospitals (open all week, longer day)
INSERT INTO simulation_profiles
 (location_id, active_counters, avg_service_minutes, base_arrivals_per_hour, weekday_factors, hourly_factors)
SELECT l.id, p.counters, p.service_min, p.arrivals,
       ARRAY[1.2, 1.0, 1.0, 1.0, 1.1, 0.8, 0.7]::real[],
       ARRAY[0,0,0,0,0,0,0, 0.7,1.2,1.5,1.4,1.2,0.9,0.8,0.9,1.0,0.9,0.8,0.7,0.5,0.3, 0,0,0]::real[]
FROM (VALUES
  ('Demo Public Clinic - Sousse',  4, 12.0, 12.0),
  ('Demo Public Hospital - Sfax',  5, 15.0, 14.0)
) AS p(name, counters, service_min, arrivals)
JOIN service_locations l ON l.name = p.name;
