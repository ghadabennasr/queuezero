-- V3: make the demo queues busier (arrival rates x 1.3)
UPDATE simulation_profiles
SET base_arrivals_per_hour = round((base_arrivals_per_hour * 1.3)::numeric, 1);
