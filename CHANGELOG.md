# [1.18.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.17.0...v1.18.0) (2026-04-19)


### Features

* configure header forwarding strategy and update nginx proxy headers for host consistency ([3520be2](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/3520be23a2ceda060cb7c512bebff152c35e02f8))

# [1.17.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.16.0...v1.17.0) (2026-04-19)


### Features

* update project documentation in README.md ([9cbe0e1](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/9cbe0e10d0dabca4e18a5a5a0e87a6b13e4058c0))

# [1.16.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.15.0...v1.16.0) (2026-04-19)


### Features

* increase client_max_body_size to 100M in nginx configuration ([6466201](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/6466201cd18da71375dd7560bb18863a80e7a3a2))

# [1.15.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.14.0...v1.15.0) (2026-04-19)


### Features

* implement autocomplete API for lead attributes with repository support and integration tests ([6830ee0](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/6830ee002e80245d350a5fc2a7e8a51ac6b25032))

# [1.14.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.13.0...v1.14.0) (2026-04-19)


### Features

* add managerId and qualification filters to SLA full summary endpoint and repository query ([2cfe298](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/2cfe298f4962c8ab6e51b83be11d04a35a156bd7))
* update SLA full cycle threshold calculation to use minutes instead of days ([a61ae0e](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/a61ae0ecaa3a4ed0998c3a2bf68cb85d0bb5fc9c))

# [1.13.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.12.0...v1.13.0) (2026-04-19)


### Features

* add SLA calculation for direct sale-to-delivery transition and update integration tests with detailed metric assertions. ([a0b8525](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/a0b85251c2b70a101d50bcd8fbe21c1efa8c394a))

# [1.12.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.11.0...v1.12.0) (2026-04-19)


### Bug Fixes

* add full summary endpoint to SlaAnalyticsController and inject SlaService into B2CSlaControllerTest ([adcf1a7](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/adcf1a749091ccd9b2415c8a8aae1065363081a3))


### Features

* add full SLA metrics endpoint ([ccda26d](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/ccda26d4819a5985bd3f9d8118822541a21ac4fe))

# [1.11.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.10.0...v1.11.0) (2026-04-18)


### Features

* introduce DateResolutionUtil and integrate date validation into SLA services and controllers ([3a32420](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/3a324207febee0d508e6ad7aed66d9df8c660e0f))

# [1.10.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.9.0...v1.10.0) (2026-04-18)


### Bug Fixes

* return SlaConfigDTO instead of raw @Configuration proxy from /api/sla/config ([679bba1](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/679bba1786bd57d2323a651b02984c4761a0cd37))


### Features

* implement DateValidationUtil and enforce date range validation across SLA controllers ([dbd8635](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/dbd8635f451ef1fadbbc7074ee61441d1ee25cb2))

# [1.9.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.8.0...v1.9.0) (2026-04-18)


### Features

* enhance global exception handling with structured ErrorResponse and additional exception types ([fa59394](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/fa593941a0ee874eeda8bdac253a6ea7e2e252e7))
* **exception:** add global exception handler for date parsing errors ([b66269b](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/b66269b4664210bb5a7fb78f0ca463fa3e80c54d))

# [1.8.0](https://github.com/mipt-ck-hackaton-2026/artreid-3/compare/v1.7.0...v1.8.0) (2026-04-18)


### Features

* add B2C SLA metrics endpoint grouped by manager ([584c2e1](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/584c2e117b03699ad0a70724f9f2546b68896428))
* implement B2C SLA by manager endpoint and refactor date range validation logic ([bdfa73b](https://github.com/mipt-ck-hackaton-2026/artreid-3/commit/bdfa73b14443363a185b7d8dff3dec957e88e520))

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
