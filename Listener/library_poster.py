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
        if pattern in file and file.endswith(".jar"):
            selected_file = file
            break

    print(f"Debug: Selected file: {selected_file}")  # Debug print
    return selected_file if selected_file else None


headers = {
    "Content-Type": "application/octet-stream",
    "Pricecxn-Auth": os.getenv("PRICECXNAUTH"),
}

selected_file = select_file('build/libs', 'Listener-')
if selected_file:
    file_path = os.path.join('build/libs', selected_file)
else:
    exit(-1)
# Make the POST request

url = "https://cdn.preiscxn.de/modules/cxn.listener.jar"
# Open the file in binary read mode
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
