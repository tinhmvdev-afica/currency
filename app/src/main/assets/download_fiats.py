import json
import os
import urllib.request

os.makedirs("fiats", exist_ok=True)

with open("fiat_seed.json", encoding="utf-8") as f:
    data = json.load(f)

currencies = data.get("supportedCurrenciesMap", {}).values()

opener = urllib.request.build_opener()
opener.addheaders = [
    ("User-Agent", "Mozilla/5.0")
]

for c in currencies:
    if c.get("status") != "AVAILABLE":
        continue

    if c.get("countryCode") == "Crypto":
        continue

    code = c.get("currencyCode")
    icon = c.get("icon")

    if not code or not icon:
        continue

    path = f"fiats/{code.lower()}.png"

    try:
        with opener.open(icon) as response:
            with open(path, "wb") as file:
                file.write(response.read())

        print(f"OK: {code} -> {path}")

    except Exception as e:
        print(f"LOI: {code} | {icon} | {e}")

print("Hoan tat!")