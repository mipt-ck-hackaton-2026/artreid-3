# [1.7.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.6.0...v1.7.0) (2026-04-18)


### Features

* **order:** correct display of stages in the order timeline ([5ce1936](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/5ce19362665534a95d09526f31e4e7142e02a521))
* **order:** implement order timeline functionality ([05565e9](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/05565e9e2455c842b957568c135d7bc311d0c3f0))

# [1.6.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.5.0...v1.6.0) (2026-04-14)


### Features

* add SLA monitoring with full metrics support ([11226d9](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/11226d99dd1c507493221dd35053484c04b4066d))
* **sla:** implement B2C first response SLA endpoints ([4b71508](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/4b7150855bb0ef57231486cc45f407346d93320e))

# [1.5.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.4.0...v1.5.0) (2026-04-14)


### Features

* **delivery:** add delivery SLA summary endpoint with native SQL ([426778b](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/426778b5069c6b1bb33eef83fb871aa58e43d5b1))

# [1.4.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.3.0...v1.4.0) (2026-04-14)


### Features

* add delivery manager, qualification, and lifecycle status fields to LeadBatchRepository upsert logic ([baf8a6e](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/baf8a6e7475f450af151e459869092ab7d3a3914))
* update SLA metrics calculation with new lead columns and granular delivery performance tracking ([b10ce76](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/b10ce769774481810e0572f8ef2e4aa0717b3348))

# [1.3.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.2.0...v1.3.0) (2026-04-14)


### Bug Fixes

* add explicit type casting to parameters in MERGE SQL statements for lead and event batch repositories ([ad3411c](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/ad3411c9f493b3d1ac12ebc73ebc0566aa791607))


### Features

* add new stage types and migrate upsert logic to MERGE syntax for lead repositories ([f499b75](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/f499b756458576b6080e9b264a11093c93b304e2))

# [1.2.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.1.0...v1.2.0) (2026-04-08)


### Bug Fixes

* **sla:** address PR [#20](https://github.com/mipt-ck-hackaton-2026/artreid-3/issues/20) review blocking comments ([422be6e](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/422be6ef266cd9e927ffbb4ba7fab254dc94b440))


### Features

* **sla:** add delivery SLA metrics endpoint grouped by manager ([#13](https://github.com/mipt-ck-hackaton-2026/artreid-3/issues/13)) ([c94232e](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/c94232ea2b7912fb0e061b049bcfeda5d0d897b5))

# [1.1.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.0.0...v1.1.0) (2026-04-07)


### Bug Fixes

* **data-load:** switch to multipart upload and add integration test ([26ce71f](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/26ce71fbcda4f85e5e1927c395ca0aa7907de827))


### Features

* add error logging to CSV processing and update timestamp parsing to handle floating-point inputs ([b57220e](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/b57220ebeb4a630bd01c171c409d3b80e670036d))
* **data-load:** add CSV import endpoint with upsert ([2e60743](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/2e607433b8f309b1eac404dc6900d700c3f62d7c))

# 1.0.0 (2026-04-04)


### Bug Fixes

* ci test ([a132a76](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/a132a76cf02433e5c07d4d865ded5e53a058a55f))
