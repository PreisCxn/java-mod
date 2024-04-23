from file_sender import upload_file, get_version_from_build_gradle

file_path = "../Listener/build/libs/Listener.jar"

url = f"https://cdn.preiscxn.de/Listener.jar?version={get_version_from_build_gradle("Listener")}"

upload_file(file_path, url)
