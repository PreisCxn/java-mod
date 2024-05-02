import json
import os

import requests


def select_file(directory, pattern):
    # Get all files in the directory
    all_files = os.listdir(directory)
    print(f"Debug: All files in directory {directory}: {all_files}")  # Debug print

    # Initialize the file to be None
    selected_file = None

    # Loop through all files
    for file in all_files:
        # Check if the file matches the pattern and does not end with -sources.jar
        if pattern in file and file.endswith(".jar") and not file.endswith("-sources.jar"):
            selected_file = file
            break

    print(f"Debug: Selected file: {selected_file}")  # Debug print
    return selected_file if selected_file else None


def get_version():
    with open("./gradle.properties") as f:
        for line in f:
            if line.startswith("mod_version"):
                version = line.split("=")[1].strip()
            if line.startswith("minecraft_version"):
                minecraft_version = line.split("=")[1].strip()

    if version and minecraft_version:
        return f"{minecraft_version}-{version}"
    elif version:
        return version
    else:
        return "1.0.0"


def get_version_from_build_gradle(module_name):
    with open(f"./{module_name}/build.gradle") as f:
        for line in f:
            if line.startswith("version"):
                version = line.split("=")[1].replace("'", "").replace('"', '').strip()
                break
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
