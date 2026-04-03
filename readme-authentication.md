# Authentication in lib-xai-api

`lib-xai-api` supports loading your xAI API key from two primary sources:

1. **Environment variable** (recommended for production, CI/CD, containers)
2. **.env file** (convenient for local development)

Both methods are handled automatically by the `XAIClientConfig` class.

## 1. Using an Environment Variable (Recommended)

Set the environment variable `XAI_API_KEY` with your API key.

```bash
# Linux / macOS
export XAI_API_KEY="xai-YourActualKeyHere1234567890abcdef"

# Windows (Command Prompt)
set XAI_API_KEY=xai-YourActualKeyHere1234567890abcdef

# Windows (PowerShell)
$env:XAI_API_KEY = "xai-YourActualKeyHere1234567890abcdef"
```

Then initialize the config:

```java
import com.xai.client.XAIClientConfig;

XAIClientConfig config = XAIClientConfig.fromEnv();
```

The library will automatically read `XAI_API_KEY` from the system environment.

### In Docker / Kubernetes / CI

```dockerfile
ENV XAI_API_KEY="xai-..."
```

or in GitHub Actions / GitLab CI:

```yaml
env:
  XAI_API_KEY: ${{ secrets.XAI_API_KEY }}
```

## 2. Using a `.env` File (Local Development)

Create a file named `.env` in the root of your project (or any parent directory):

```env
# .env
XAI_API_KEY=xai-YourActualKeyHere1234567890abcdef
# Optional: base URL if you need a custom/regional endpoint
# XAI_BASE_URL=https://api.x.ai/v1
```

The library uses the following lookup order (first match wins):

1. System environment variable `XAI_API_KEY`
2. `.env` file loaded via `dotenv-java`-style parsing (built-in lightweight loader)
3. `~/.xai.env` (user home directory — optional fallback)
4. System properties: `-DXAI_API_KEY=...` (via `-D` JVM flag)

Initialize exactly the same way:

```java
XAIClientConfig config = XAIClientConfig.fromEnv();
```

> **Security note**:  
> Never commit `.env` files to version control.  
> Add `.env` and `*.env` to `.gitignore`.

## 3. Manual / Explicit Configuration

For full control (e.g. tests, multiple keys, hard-coded values):

```java
XAIClientConfig config = XAIClientConfig.builder()
    .apiKey("xai-YourActualKeyHere1234567890abcdef")
    .baseUrl("https://api.x.ai/v1")           // optional, defaults to official
    .connectTimeout(Duration.ofSeconds(10))   // optional
    .readTimeout(Duration.ofSeconds(60))      // optional
    .build();
```

## 4. Supported Configuration Properties

| Property             | Source                          | Description                                      | Default                          |
|----------------------|---------------------------------|--------------------------------------------------|----------------------------------|
| `XAI_API_KEY`        | env / .env / -D                 | Bearer token for all requests                    | (required)                       |
| `XAI_BASE_URL`       | env / .env / -D                 | Base API endpoint                                | `https://api.x.ai/v1`            |
| `XAI_CONNECT_TIMEOUT`| env / builder (seconds)         | Connection timeout                               | 10 seconds                       |
| `XAI_READ_TIMEOUT`   | env / builder (seconds)         | Read / response timeout                          | 60 seconds                       |
| `XAI_RETRY_ENABLED`  | env (true/false) / builder      | Automatic retries on 5xx and rate-limit errors   | true                             |
| `XAI_MAX_RETRIES`    | env / builder                   | Maximum retry attempts                           | 3                                |

Example with environment variables for timeouts:

```bash
export XAI_CONNECT_TIMEOUT=15
export XAI_READ_TIMEOUT=90
export XAI_MAX_RETRIES=5
```

## 5. Troubleshooting Authentication

| Symptom                              | Likely Cause                              | Fix                                           |
|--------------------------------------|-------------------------------------------|-----------------------------------------------|
| 401 Unauthorized                     | Missing / invalid API key                 | Check `XAI_API_KEY` value & format            |
| Config.fromEnv() throws exception    | No key found in any source                | Set env var or create `.env` file             |
| Requests timeout quickly             | Network / proxy issue or small timeout    | Increase `XAI_READ_TIMEOUT`                   |
| Intermittent 429 / 5xx failures      | Rate limiting                             | Library retries automatically (configurable)  |

## Best Practices

- **Production** → always use environment variables or secret managers (AWS Secrets, HashiCorp Vault, GitHub Secrets, etc.)
- **Local dev** → `.env` file + `.gitignore`
- **Tests** → use explicit builder or `@BeforeEach` to inject mock/fake keys
- Never log or expose the full API key — `XAIClientConfig` redacts it in `toString()`

Happy authenticating — now go build something powerful with Grok!

Questions or missing features? Open an issue or PR.

