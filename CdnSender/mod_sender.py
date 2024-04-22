import os
from file_sender import upload_file, get_version


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


selected_file = select_file('../build/libs', 'pricecxn')
if selected_file:
    file_path = os.path.join('../build/libs', selected_file)
else:
    exit(-1)
# Make the POST request

url = f"https://cdn.preiscxn.de/PriceCxnMod.jar?version={get_version()}"
upload_file(file_path, url)
