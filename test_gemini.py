import requests

session = requests.Session()
register_data = {
    'name': 'Test User',
    'age': 30,
    'email': 'test3@test.com',
    'password': 'password',
    'parentGuardianNumber': '+1234567890'
}
r1 = session.post('http://localhost:8081/register', data=register_data)
print('Register status:', r1.status_code)

login_data = {
    'email': 'test3@test.com',
    'password': 'password'
}
r2 = session.post('http://localhost:8081/login', data=login_data)
print('Login status:', r2.status_code)

query_data = {
    'query': 'Hello, are you there?'
}
r3 = session.post('http://localhost:8081/api/medical/query', json=query_data)
print('Query status:', r3.status_code)
print('Query response:', r3.text)
