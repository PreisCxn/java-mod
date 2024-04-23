from file_sender import upload_file, get_version_from_build_gradle, select_file

file_path = select_file('./Listener/build/libs', 'Listener')


version = get_version_from_build_gradle("Listener")
url = f"https://cdn.preiscxn.de/Listener.jar?version={version}"

upload_file(file_path, url)
