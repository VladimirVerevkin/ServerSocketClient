# python3 

import socket
import keyboard
import win10toast
import threading

#ip_server = '127.0.0.1'
#ip_server = '10.97.226.91'
#ip_server = 'KRA52-INGEN07'
port_server = 9090

# метод всплывающее окно для информирования
def info_message(data_title, data_info):
    toaster = win10toast.ToastNotifier()
    toaster.show_toast(data_title, data_info)

#метод разделения строки на фрагметы: command - команда, данные 
def decode_in_data(data):
    # Разбиваем строку на массив на каждом пробеле. Например "dl dz" превратится в ['dl', 'gz'].
    data = data.decode("utf-8").split()

    #result = 0      #reset = 0 - сброс приема данных

    if data[0] == 'barCode':
        print('прием штрих код: ' + str(data[1:]))
        result0 = 1             # 1 - принят штрих код
        result1 = data[1:]      # штрих код
    elif data[0] == 'File':
        print('прием Файла: Путь ' + str(data[1:]))
        result0 = 2             # 2 - прием файла
        result1 = data[1:]      # путь+имя файла
    elif data[0] == 'endDataTransfer':
        result0 = 3             # 3 - прием файла\файлов или др. данных закончен
        result1 = ''
    return result0, result1;

# получение IP адрес компьютера
hostnamePC = socket.gethostname()
ip_server = socket.gethostbyname(hostnamePC)
print(ip_server)
try:
    serv_sock = socket.socket(socket.AF_INET,       # задамем семейство протоколов 'Интернет' (INET)
                              socket.SOCK_STREAM,   # задаем тип передачи данных 'потоковый' (TCP)
                              proto=0)              # выбираем протокол 'по умолчанию' для TCP, т.е. IP <class 'socket.socket'>

    serv_sock.bind((ip_server, port_server))
    serv_sock.listen(2)
except:
    print('сервер не запускается или уже запущен')
    info_message('Ошибка', ' Сервер уже запущен')
    
# ожидание данных от "клиента"
while True:
    try:
        client_sock, client_addr = serv_sock.accept() #новый сокет и адрес клиента, для приема и посылке клиенту данных
        print("Receiving...")
        data = client_sock.recv(1024)   #чтение 1024 байт переданных данных от клиента
        decode = decode_in_data(data)
    
        if decode[0] == 1:       # 1 - прием штрих кода, обработка данных с штрих кодом
            barCode = ''.join(decode[1])
            print('Принят штрих код: ' + barCode)
        elif decode[0] == 2:      #2 - прием файла, обработка приема файла 
            track_name_file = ''.join(decode[1])
            #print(track_name_file)
            filetodown = open(track_name_file,'wb')       # открать(создать) файл track_name_file - путь+имя файла
            data = client_sock.recv(1024)           # чтение 1024байт переданных данных от клиента
            while data:                             # прием файла по 1024 байта
                filetodown.write(data)              # запись данных в файл
                data = client_sock.recv(1024)       # чтение 1024байт переданных данных от клиента
            filetodown.close()                      # закрыть файл
            print('файл принят')
        elif decode[0] == 3:    # 3 - прием данных информирующих о завершении передачи данных
            print('Передача данных закончена')
            info_message('Загрузка', 'Загрузка файлов закончена')
    except:
        print('Ошибка, закрытие сокета')
        client_sock.close()
        
client_sock.close()

