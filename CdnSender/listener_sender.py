from file_sender import upload_file, get_version

file_path = "../Listener/build/libs/Listener.jar"

url = f"https://cdn.preiscxn.de/Listener.jar?version={get_version()}"
# Open the file in binary read mode

upload_file(file_path, url)
