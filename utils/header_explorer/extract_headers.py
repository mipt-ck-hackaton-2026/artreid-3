import csv
import os

def extract_headers(input_file, output_file):
    if not os.path.exists(input_file):
        print(f"Error: {input_file} not found.")
        return

    try:
        with open(input_file, mode='r', encoding='utf-8') as f:
            # Using csv.reader to handle potential quoting in headers
            reader = csv.reader(f)
            headers = next(reader)
        
        with open(output_file, mode='w', encoding='utf-8') as f:
            f.write(f"Headers for {input_file}:\n")
            f.write("=" * (len(input_file) + 13) + "\n\n")
            for i, header in enumerate(headers, 1):
                f.write(f"{i}. {header}\n")
        
        print(f"Successfully extracted {len(headers)} headers to {output_file}")
    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    csv_filename = "dataset.csv"
    txt_filename = "dataset_headers.txt"
    extract_headers(csv_filename, txt_filename)
