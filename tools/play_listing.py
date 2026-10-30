"""Push the en-US store listing for Comfort Gauge to Google Play (text, icon, feature graphic, phone screenshots).

Run with the shared publisher venv:
  C:\\_Code\\Android\\_play-publisher\\.venv\\Scripts\\python tools\\play_listing.py [--dry-run]

Everything is done in one edit; on any error the edit is deleted so Play is never left half-changed.
"""
import argparse
import pathlib
import sys

sys.path.insert(0, r"C:\_Code\Android\_play-publisher")
from play_publish import service  # noqa: E402
from googleapiclient.errors import HttpError  # noqa: E402
from googleapiclient.http import MediaFileUpload  # noqa: E402

PACKAGE = "com.root3.comfortgauge"
LANG = "en-US"
LISTING = pathlib.Path(__file__).resolve().parent.parent / "_docs" / "play-store-listing"
DETAILS = {
    "contactEmail": "kjcornwell@gmail.com",
    "contactWebsite": "https://kjcornwell.github.io/ComfortCalculator/",
    "defaultLanguage": LANG,
}


def read(name):
    text = (LISTING / name).read_text(encoding="utf-8").strip()
    if not text:
        sys.exit(f"{name} is empty")
    return text


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true", help="validate locally, change nothing on Play")
    args = ap.parse_args()

    short, full = read("short.txt"), read("full.txt")
    if len(short) > 80 or len(full) > 4000:
        sys.exit(f"Listing too long: short={len(short)}/80 full={len(full)}/4000")
    shots = sorted(LISTING.glob("phone-*.png"))
    images = {"icon": [LISTING / "icon-512.png"], "featureGraphic": [LISTING / "feature.png"], "phoneScreenshots": shots}
    for kind, files in images.items():
        missing = [str(f) for f in files if not f.is_file()]
        if missing or not files:
            sys.exit(f"Missing {kind} image(s): {missing or 'none found'}")
    print(f"short={len(short)} full={len(full)} screenshots={len(shots)}")
    if args.dry_run:
        return

    edits = service().edits()
    edit_id = edits.insert(packageName=PACKAGE, body={}).execute()["id"]
    try:
        edits.details().update(packageName=PACKAGE, editId=edit_id, body=DETAILS).execute()
        edits.listings().update(
            packageName=PACKAGE, editId=edit_id, language=LANG,
            body={"language": LANG, "title": "Comfort Gauge", "shortDescription": short, "fullDescription": full},
        ).execute()
        for kind, files in images.items():
            edits.images().deleteall(packageName=PACKAGE, editId=edit_id, language=LANG, imageType=kind).execute()
            for f in files:
                edits.images().upload(
                    packageName=PACKAGE, editId=edit_id, language=LANG, imageType=kind,
                    media_body=MediaFileUpload(str(f), mimetype="image/png"),
                ).execute()
                print(f"uploaded {kind}: {f.name}")
        edits.commit(packageName=PACKAGE, editId=edit_id).execute()
        print("committed")
    except HttpError as e:
        edits.delete(packageName=PACKAGE, editId=edit_id).execute()
        sys.exit(f"Play API error, edit deleted (Play unchanged): {e}")


if __name__ == "__main__":
    main()
