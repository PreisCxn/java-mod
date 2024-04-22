import json
import os

import requests


def get_version():
    with open("../gradle.properties") as f:
        for line in f:
            if line.startswith("mod_version"):
                version = line.split("=")[1].strip()

    return version if version else "1.0.0"


def upload_file(file_path, url):
    headers = {
        "Content-Type": "application/octet-stream",
        "Pricecxn-Auth": os.getenv("PRICECXNAUTH"),
    }

    with open(file_path, 'rb') as f:
        # Start the upload session and get the file ID
        response = requests.post(url, headers=headers)
        if response.status_code != 200:
            print(f"Failed to start upload session. Status code: {response.status_code}")
            exit(-1)
        file_id = json.loads(response.text)['file-id']

        # Iterate over the file in chunks and stream the data
        for chunk in iter(lambda: f.read(128 * 1024), b''):
            # Make the POST request with chunked data
            response = requests.post(url, headers={**headers, 'file-id': str(file_id)}, data=chunk)

            # Check the response
            if response.status_code != 200:
                print(f"Failed to upload chunk. Status code: {response.status_code}")
                print(response)
                exit(-1)

        response = requests.post(url, headers={**headers, 'file-id': str(file_id)}, data=None)
        if response.status_code != 200:
            print(f"Failed to send empty POST request. Status code: {response.status_code}")
            print(response)
            exit(-1)

    print("File uploaded successfully.")
