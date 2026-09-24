# TempoStrike server

FastAPI service for user profiles, training sessions, achievements, and leaderboards. The source is derived from HITRISE, but its runtime resources are isolated.

## Production layout

- MySQL database: `tempostrike` (`sql/schema.sql`)
- MySQL application user: `tempostrike_app`, with DML grants only on `tempostrike.*`
- Docker container: `tempostrike-auth`, internal port `8016`, network `gitlab-net`
- HTTPS endpoint: `https://hitrise.86086.cn/tempostrike/`
- Nginx route: `deploy/nginx/tempostrike-location.conf` inside the existing `hitrise.86086.cn` server block
- Runtime data: `/home/ubuntu/tempostrike/uploads`
- Protected runtime secrets: `/home/ubuntu/tempostrike/tempostrike.env` (mode `0600`)

The Nginx `/hitrise/` route and HITRISE container/database remain separate. The legacy deployment scripts were removed because they targeted the HITRISE service.

`deploy/tempostrike.env.example` lists required environment variables without credentials. The Docker Compose file describes the independent container for future deployments. Do not commit the production environment file or the server parameter attachment.

## Verification

Check both `https://hitrise.86086.cn/tempostrike/health` and `https://hitrise.86086.cn/hitrise/health` after changes. The responses should name `TempoStrike` and `hitrise-auth`, respectively.
