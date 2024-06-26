import os
from file_sender import upload_file, get_version, select_file


selected_file = select_file('./fabric/build/libs', 'fabric')
if selected_file:
    file_path = os.path.join('./fabric/build/libs', selected_file)
else:
    exit(-1)
# Make the POST request

url = f"https://cdn.preiscxn.de/PriceCxnMod.jar?version={get_version()}"
upload_file(file_path, url)
