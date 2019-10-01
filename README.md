# AppCivil
Esta aplicação Android permite ver que salas estão livres para estudar no Pavilhão de Civil do Instituto Superior Técnico.

Selecionando a hora atual, ou uma hora em específico (do próprio dia), a aplicação mostra:
- As salas disponíveis no pavilhão de civil para estudar a essa hora e por quanto tempo irão estar disponíveis.
- Quanto tempo falta para as restantes salas ficarem disponíveis.

É também possível adicionar salas favoritas, carregando no ícone de coração ao pé do nome de cada sala. Esse ícone fica verde se a sala for favorita, e transparente com contorno verde se não o for.

As salas são mostradas por ordem, aparecendo primeiro as salas favoritas (pela ordem descrita de seguida), depois as salas disponíveis por ordem decrescente de tempo disponível e, de seguida, aparecem as salas indisponíveis nesse momento, por ordem crescente de tempo que falta para estarem disponíveis.

A API utilizada para obter o eventos de cada sala, foi a API [Get Spaces](https://fenixedu.org/dev/api/#get-spaces) da Fenix EDU, e a  API para dar o estado de favorito às salas foi a [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences#developer-guides).
