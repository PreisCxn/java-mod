import os

from file_sender import upload_file, select_file, get_version

selected_file = select_file('./listener/build/libs', 'listener')

if selected_file:
    file_path = os.path.join('./listener/build/libs', selected_file)
else:
    exit(-1)

url = f"https://cdn.preiscxn.de/Listener.jar?version={get_version(False)}"

upload_file(file_path, url)
