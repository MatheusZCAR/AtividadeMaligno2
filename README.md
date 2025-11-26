# README – Trabalho 2
# Ordenação Distribuída com Merge Sort Paralelo

## Comandos de Compilação e Execução

### 1. Compilar o código
javac -d bin src/*.java

### 2. Executar o servidor R (Receptor)
java -cp bin R

### 3. Executar o cliente Distribuidor
java -cp bin Distribuidor

### 4. Executar a versão Sequencial (para comparação)
java -cp bin OrdenacaoSequencial


# O que aconteceu na demonstração

## Servidor R (Receptor):
- Iniciou corretamente na porta 12345
- Detectou corretamente a quantidade de processadores disponíveis
- Recebeu partes do vetor enviadas pelo Distribuidor
- Ordenou cada subvetor em paralelo com múltiplas threads
- Realizou merge interno das partes ordenadas
- Enviou o subvetor ordenado de volta ao cliente
- Recebeu e processou ComunicadoEncerramento sem erros

## Cliente Distribuidor:
- Gerou o vetor grande de bytes
- Dividiu o vetor entre os servidores
- Criou threads clientes para enviar subvetores
- Recebeu subvetores ordenados dos R
- Fez o merge final paralelo no cliente
- Salvou o vetor final ordenado em um arquivo .txt fornecido pelo usuário
- Enviou ComunicadoEncerramento para todos os servidores ao finalizar
- Em um teste houve erro de entrada de dados do usuário (esperava número inteiro), mas não comprometeu a lógica principal


# Demonstração do comportamento distribuído e paralelo

1. Ordenação Distribuída  
   O vetor é dividido e enviado para diferentes servidores ordenarem simultaneamente.

2. Merge Sort Paralelo nos Servidores (R)  
   Cada servidor divide sua parte em blocos menores, cria threads ordenadoras e threads juntadoras.

3. Merge Final Paralelo no Cliente (Distribuidor)  
   Os subvetores chegam ordenados e o cliente executa merges 2 a 2 também paralelos.

4. Conexões Independentes  
   Cada cliente cria sua própria conexão com o R e é atendido separadamente.

5. Encerramento Limpo  
   O cliente envia ComunicadoEncerramento para cada servidor antes de encerrar o programa.


# Como testar com dados reais

Para enviar automaticamente um vetor grande, por exemplo de 1 milhão de elementos:

echo "1000000" | java -cp bin Distribuidor


# Relatos dos Integrantes

## Matheus:
O desenvolvimento deste segundo projeto ampliou bastante os desafios do primeiro. A introdução do Merge Sort paralelo nos servidores e do merge final paralelo no distribuidor exigiu uma boa compreensão da decomposição do problema e do uso coordenado de threads. Além disso, gerenciar sockets enquanto várias threads estavam ativas trouxe um alto nível de atenção, especialmente ao garantir que cada conexão fosse encerrada de forma limpa. A maior dificuldade foi garantir que todas as partes ordenadas se unissem corretamente no final. Os testes foram essenciais para detectar erros de merge e problemas de concorrência. No final, o trabalho consolidou fortemente minha compreensão de distribuição de tarefas e ordenação paralela.

## Manoel:
A evolução do Trabalho 1 para o Trabalho 2 representou um aumento significativo na complexidade. Implementar a comunicação básica novamente foi simples, mas adaptar a lógica para ordenação paralela exigiu um estudo mais profundo. Dividir vetores, sincronizar merges, garantir que cada thread operasse sem conflitos — tudo isso se mostrou um excelente desafio. O aprendizado principal foi sobre o impacto das estratégias de paralelização no desempenho e como pequenos ajustes podem gerar grandes diferenças. Foi uma oportunidade valiosa para reforçar conceitos de programação concorrente e sistemas distribuídos.

## Beatriz:
Participar deste projeto foi uma experiência essencial para compreender o funcionamento real de sistemas distribuídos. No início, o conceito de dividir um problema em subpartes que seriam enviadas a diferentes servidores parecia abstrato, mas ao longo da implementação passou a fazer sentido de forma prática. A parte mais desafiadora foi garantir que o merge dos vetores retornados pelos servidores fosse feito corretamente e de maneira paralela, preservando a ordem. Também aprendi bastante sobre comunicação via sockets, especialmente a importância do uso correto de streams e do envio explícito do ComunicadoEncerramento. Além disso, trabalhar com múltiplas threads ao mesmo tempo exigiu bastante atenção para evitar conflitos e erros inesperados. No geral, o projeto contribuiu muito para minha evolução em programação concorrente e distribuições de tarefas.
