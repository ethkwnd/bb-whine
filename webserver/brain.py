

def brain(file_path: str):
    status_code = 400
    with open(file_path, "r") as file:
        if file:
            status_code = "adsad"
    return status_code
