import grpc

from src.main.proto.service import service_count_letters_pb2
from src.main.proto.service import service_count_letters_pb2_grpc

def run_client(text_to_count):
    server_address = 'localhost:50051' 

    with grpc.insecure_channel(server_address) as channel:
        stub = service_count_letters_pb2_grpc.CountLettersServiceStub(channel)
        
        request_message = service_count_letters_pb2.InputString(input_string=text_to_count)
        
        print(f"Cliente Python: Enviando '{text_to_count}'...")
        response = stub.CountLetters(request_message) 
        
        print(f"Cliente Python: El servidor contó {response.letter_number} letras.")
        return response.letter_number


if __name__ == '__main__':
    text = "trying server"
    resultado = run_client(text)
    if resultado is not None:
        print(f"Resultado final para '{text}': {resultado}")