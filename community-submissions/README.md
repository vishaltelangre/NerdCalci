# Community submissions

A collection of community-contributed NerdCalci files ("calcis") that showcase
reusable calculations, domain-specific helpers, and handy formulas.

## What is a calci?

A **calci** is a plain-text calculation file written in NerdCalci's expression language. Each line is either:

- A blank line or comment (`# ...`)
- A variable assignment (`name = expr`)
- A user-defined function (`name(args) = body`)
- A standalone expression

The [NerdCalci reference guide](../REFERENCE.md) documents every feature you
can use inside a calci.

These calcis can be directly copy-pasted (or imported) in the NerdCalci app.

> [!IMPORTANT]
> **Disclaimer**: These calcis are community-contributed and are not officially
> endorsed by the NerdCalci author. You are advised to review and test these
> calcis before using them. Any issues, problems or bugs arising from the use
> of these calcis are solely the responsibility of the user.

## How to submit

1. Fork the [NerdCalci repository](https://github.com/vishaltelangre/NerdCalci).
2. Create your calci inside this `community-submissions/` directory.
   - File name: `<topic>.nerdcalci` (e.g. `geometry.nerdcalci`,
     `personal_finance.nerdcalci`).
   - One file per focused topic.
3. Write valid expressions only.
   Every non-blank, non-comment line must produce a result (not `Err`).
   The automated test suite (`CommunityCalciTest`) enforces this.
4. Add a top-level comment block at the start of your file with:
   - A short description of what the calci covers.
   - Your GitHub username (optional).
5. Open a Pull Request.

### File structure guidelines

```text
# <Short description of the calci>
# Author: @your-github-username (optional)

variable = expression
result_variable = some_formula(variable)
```

---

## Running the tests locally

```bash
./gradlew :app:testDebugUnitTest --tests "com.vishaltelangre.nerdcalci.core.CommunityCalciTest"
```
