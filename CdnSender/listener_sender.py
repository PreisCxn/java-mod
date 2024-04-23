import os

from file_sender import upload_file, get_version_from_build_gradle, select_file

selected_file = select_file('./Listener/build/libs', 'Listener')

if selected_file:
    file_path = os.path.join('./Listener/build/libs', selected_file)
else:
    exit(-1)

version = get_version_from_build_gradle("Listener")
url = f"https://cdn.preiscxn.de/Listener.jar?version={version}"

upload_file(file_path, url)
