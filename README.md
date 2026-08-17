# WakePulse

WakePulse é um aplicativo Android local e sem root que agenda pequenos “pulsos” para despertar periodicamente a CPU durante o repouso. O objetivo é reduzir — não eliminar nem garantir a eliminação — de notificações atrasadas enquanto o aparelho está em Doze.

O app não usa Internet, não lê notificações, não contém analytics, anúncios ou SDKs de terceiros e não envia dados para serviços externos.

## Como funciona

Quando a proteção é ativada, o WakePulse:

1. cancela qualquer `PendingIntent` de pulso anterior;
2. calcula o próximo disparo com `SystemClock.elapsedRealtime()`;
3. agenda um `AlarmManager.ELAPSED_REALTIME_WAKEUP` usando `setExactAndAllowWhileIdle()`;
4. se o acesso especial a alarmes exatos não estiver concedido, usa `setAndAllowWhileIdle()` como fallback menos preciso;
5. ao disparar, o `PulseReceiver` adquire um `PARTIAL_WAKE_LOCK` por cerca de 5 segundos, com timeout rígido de 8 segundos;
6. registra horário, contador e histórico no DataStore;
7. reageenda explicitamente o próximo pulso.

Não é usado alarm repeating. Cada disparo gera exatamente o próximo agendamento. Também não existe Foreground Service: o trabalho local é curto e cabe no ciclo assíncrono de um `BroadcastReceiver`, portanto um serviço persistente adicionaria consumo e uma notificação permanente sem benefício proporcional.

## O que é Doze Mode

Doze é o mecanismo do Android que reduz atividade de CPU, rede, jobs e alarmes quando o aparelho permanece parado, desconectado e com a tela desligada. Durante manutenção restrita, o Android agrupa ou adia trabalho para economizar bateria.

Notificações push normalmente dependem do Firebase Cloud Messaging (FCM) e da prioridade escolhida pelo aplicativo de destino. Mensagens de alta prioridade podem atravessar o Doze, mas o Android pode rebaixar uso abusivo; mensagens normais podem esperar uma janela de manutenção. Fabricantes também aplicam políticas próprias de bateria, rede e encerramento de processos.

O pulso do WakePulse cria uma breve oportunidade de execução da CPU. Isso pode ajudar em alguns aparelhos e cenários, mas não força WhatsApp, Telegram, Gmail ou qualquer outro app a sincronizar, não abre a rede por conta própria e não controla o FCM.

### FCM e por que o WakePulse não controla a entrega

O Firebase Cloud Messaging tem prioridade **normal** e **high**. Normal é a prioridade padrão de muitas mensagens e pode ficar armazenada durante Doze até uma janela de manutenção. High tenta acordar o aparelho e concede ao aplicativo de destino alguns segundos de processamento e acesso de rede muito limitado. Quem escolhe essa prioridade é o servidor do app de mensagens, não o usuário e não o WakePulse.

O FCM avalia aproximadamente sete dias de comportamento por instância. Se mensagens high não resultarem em notificações visíveis, o Google pode rebaixá-las a normal ou delegar a exibição ao Google Play Services. Mesmo depois da entrega FCM, o aplicativo precisa processar o payload e publicar a notificação dentro da janela permitida. Chamadas extras de rede, permissão de notificação negada, canal bloqueado ou processo restrito podem gerar atraso/falha.

Consequentemente, um WakeLock do WakePulse não concede essa janela ao WhatsApp/Gmail/Telegram. O app é um mitigador experimental e um instrumento de diagnóstico, não uma substituição do mecanismo FCM. Consulte [prioridade de mensagens no Android](https://firebase.google.com/docs/cloud-messaging/android-message-priority) e [prioridade de entrega FCM](https://firebase.google.com/docs/cloud-messaging/customize-messages/setting-message-priority).

## Limitações importantes

- `setExactAndAllowWhileIdle()` não significa frequência irrestrita. O Android aplica throttling por aplicativo durante o idle e pode espaçar os disparos mais do que o intervalo solicitado.
- A [documentação oficial do AlarmManager](https://developer.android.com/develop/background-work/services/alarms) avisa que alarmes exatos consomem recursos e devem ser reservados a funções visíveis ao usuário.
- Em instalações novas no Android 14+ com target moderno, `SCHEDULE_EXACT_ALARM` normalmente começa negada. O usuário precisa liberar “Alarmes e lembretes”.
- Se a permissão exata for revogada, o Android cancela alarmes exatos futuros. O WakePulse detecta o estado na UI e reageenda ao receber a concessão novamente.
- O fallback `setAndAllowWhileIdle()` pode atrasar sensivelmente, sobretudo com economia de bateria, Doze profundo ou restrições OEM.
- Reinicialização, force-stop, revogação de permissões, “limpeza” do fabricante ou desinstalação podem interromper agendamentos. Após `BOOT_COMPLETED`, o app restaura somente se estava ativo antes do reboot. Um force-stop explícito impede receivers até o usuário abrir o app novamente, por regra do Android.
- WakePulse não garante 100% das notificações. FCM, prioridade da mensagem, aplicativo de destino, conectividade, fabricante, modo de economia e otimizações OEM continuam determinantes.

## Intervalos e bateria

| Intervalo | Perfil | Observação |
|---|---|---|
| 5 min | Experimental | Mais agressivo; maior chance de throttling e maior consumo. O Android pode alongá-lo. |
| 9 min | Recomendado | Compromisso inicial entre oportunidade de despertar e bateria. Ainda pode ser limitado. |
| 15 min | Equilibrado | Menos despertares e impacto moderado. |
| 30 min | Econômico | Menor impacto, mas janela maior para atrasos. |

O impacto real varia muito por aparelho. Cada pulso mantém apenas um WakeLock parcial curto, mas despertar a CPU repetidamente impede parte da economia do Doze. Compare o consumo em **Configurações → Bateria → Uso da bateria** por alguns ciclos completos antes de manter a opção de 5 minutos.

## Recursos da interface

- ativar/desativar a proteção;
- escolher 5, 9, 15 ou 30 minutos;
- ver último pulso, próximo horário aproximado, contador e intervalo;
- consultar e abrir as configurações de exact alarms;
- consultar e abrir a confirmação de exclusão da otimização de bateria;
- diagnóstico de `isDeviceIdleMode()`, `isInteractive()`, exact alarm e bateria;
- executar um pulso manual imediato;
- executar um autoteste real de AlarmManager para ~60 segundos;
- pausar WakeLocks enquanto Não Perturbe/Modo Sono estiver ativo;
- histórico local dos 50 pulsos mais recentes;
- Material 3, cores dinâmicas, dark mode e layout responsivo.

## Idiomas

O WakePulse inclui recursos completos para **English (United States)** e **Português (Brasil)**. Inglês é o idioma padrão de fallback e o Android seleciona automaticamente o português quando o aparelho ou o idioma do aplicativo está configurado como `pt-BR`.

No Android 13 e versões mais recentes, os dois idiomas aparecem no seletor de idioma por aplicativo em **Configurações → Apps → WakePulse → Idioma**. Em versões anteriores, o app acompanha o idioma do sistema.

## Permissões

| Permissão | Motivo |
|---|---|
| `WAKE_LOCK` | Adquirir `PARTIAL_WAKE_LOCK` temporário durante o pulso. É permissão normal. |
| `RECEIVE_BOOT_COMPLETED` | Restaurar o alarme depois do boot se o mecanismo já estava ativo. |
| `SCHEDULE_EXACT_ALARM` | Solicitar acesso especial, controlado pelo usuário, para `setExactAndAllowWhileIdle()`. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Abrir a confirmação do sistema para excluir somente o WakePulse da otimização. O app não concede sozinho. |
| `ACCESS_NOTIFICATION_POLICY` | Detectar mudanças do Não Perturbe para pausar durante o sono. Não concede acesso ao conteúdo das notificações. |

Não existe `INTERNET`, acesso a notificações, contatos, arquivos, localização, microfone ou identificadores.

## Arquitetura

```text
UI Compose / WakePulseViewModel
        │
        ├── PulseController ── PulseScheduler ── AndroidAlarmGateway
        │                               │              │
        │                               │          AlarmManager
        │                               │
        └── PulseExecutor ──────────────┘
                │
          PARTIAL_WAKE_LOCK
                │
        AndroidPulsePreferences (DataStore)

PulseReceiver ── PulseExecutor ── reagenda próximo pulso
BootReceiver  ── RestoreAfterBoot ── agenda somente se ativo
ExactAlarmPermissionReceiver ── restaura alarme após concessão
```

As principais classes ficam em:

- `alarm/PulseScheduler.kt`: serialização, cancelamento e escolha exact/fallback;
- `receiver/PulseReceiver.kt`: entrada do alarme;
- `pulse/PulseExecutor.kt`: WakeLock com timeout, registro e reagendamento;
- `data/AndroidPulsePreferences.kt`: estado e histórico no DataStore Preferences;
- `receiver/BootReceiver.kt`: restauração após reboot;
- `system/SystemStatusProvider.kt`: leituras de idle, tela, exact alarm e bateria;
- `ui/`: Compose, tema e ViewModel.

## Requisitos de build

- Android Studio compatível com AGP 9.3 ou linha de comando;
- JDK 17;
- Android SDK Platform 37;
- Build Tools 36.0.0;
- Gradle Wrapper 9.5 (incluído).

O projeto usa Kotlin, Gradle Kotlin DSL, Jetpack Compose, Material 3, `minSdk 26`, `compileSdk 37` e `targetSdk 37`. Ele adota as regras atuais do Android 17 e continua instalável no Android 8+ e no One UI baseado em Android 16.

## Modo Sono, Não Perturbe e One UI

A Samsung não publica uma API estável para um app comum consultar diretamente o estado interno de **Modos e Rotinas → Sono**. O WakePulse usa a ponte oficial do Android: `NotificationManager.currentInterruptionFilter`. Normalmente o Modo Sono da One UI ativa Não Perturbe, portanto a pausa acompanha essa mudança.

Com **Pausar no Não Perturbe** ligado (padrão):

- o WakePulse não adquire WakeLock nem conta um pulso enquanto DND estiver ativo;
- uma checagem curta continua no intervalo escolhido para que a retomada seja robusta mesmo se o processo tiver sido encerrado;
- quando o processo está vivo, o broadcast de mudança do DND reageenda imediatamente após o fim do modo;
- qualquer perfil de Não Perturbe provoca pausa, pois a API pública não identifica de modo confiável que a origem específica foi “Sono”;
- o usuário pode desligar essa proteção na tela principal.

Conceda **Acesso ao Não Perturbe** pelo botão do app. Esse acesso observa a política global; WakePulse não lê, intercepta, remove ou modifica notificações e nunca liga/desliga o DND.

No One UI 8.x, confira também **Configurações → Assistência do aparelho e bateria → Bateria → Limites de uso em segundo plano** e não deixe o WakePulse em “Apps em suspensão profunda”. Os nomes podem variar por idioma/versão. A exclusão de otimização padrão do Android e a lista própria da Samsung são controles diferentes.

### Android Studio

1. Abra a pasta raiz `wakepulse`.
2. Selecione JDK 17 em **Settings → Build Tools → Gradle → Gradle JDK**.
3. Instale Android SDK 37 pelo SDK Manager, se solicitado.
4. Aguarde o Gradle Sync.
5. Selecione um aparelho Android 8.0+ e execute `app`.

### Linha de comando

Configure o SDK e rode:

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export JAVA_HOME="/caminho/para/jdk-17"
./gradlew clean assembleDebug
```

Em uma instalação Homebrew Apple Silicon, o SDK também pode estar em `/opt/homebrew/share/android-commandlinetools`.

O APK debug é criado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Instalar via ADB

Ative a depuração USB, conecte o aparelho e execute:

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Depois, abra WakePulse e use os botões da própria interface para conceder acesso a alarmes exatos e revisar a otimização de bateria. Nada é concedido silenciosamente.

## Testes automatizados

Testes unitários cobrem cálculo do próximo pulso, sanitização do intervalo, ativação/desativação, cancelamento e scheduling exact/fallback, mudança de intervalo e restauração após boot:

```bash
./gradlew testDebugUnitTest
```

Há um teste instrumentado do DataStore e do limite do histórico:

```bash
./gradlew connectedDebugAndroidTest
```

O segundo comando requer emulador ou aparelho conectado.

## Testar Doze via ADB

Use um aparelho de teste. Políticas e comandos podem variar por versão/OEM.

1. Ative o WakePulse e confirme no app que existe um próximo pulso.
2. Desligue a tela.
3. Force idle:

   ```bash
   adb shell dumpsys deviceidle force-idle
   ```

4. Confira o estado:

   ```bash
   adb shell dumpsys deviceidle
   ```

5. Avance manualmente a máquina de estados, quando necessário:

   ```bash
   adb shell dumpsys deviceidle step
   ```

6. Inspecione alarmes do pacote:

   ```bash
   adb shell dumpsys alarm | grep -A 12 com.wakepulse.app
   ```

7. Observe logs:

   ```bash
   adb logcat -s WakePulse
   ```

   Ou, em shells com `grep`:

   ```bash
   adb logcat | grep WakePulse
   ```

8. Sempre saia do force idle ao terminar:

   ```bash
   adb shell dumpsys deviceidle unforce
   ```

   Ligue/desbloqueie a tela em seguida. Se necessário, confirme com outro `adb shell dumpsys deviceidle`.

### Autoteste de 60 segundos no aparelho

1. Abra **Diagnóstico**.
2. Confirme `canScheduleExactAlarms() = true`.
3. Toque em **Testar AlarmManager em ~60 segundos**.
4. Desligue a tela sem fechar à força o app.
5. Aguarde cerca de 60 segundos. O sistema ainda pode aplicar pequeno atraso.
6. Volte ao app: contador/histórico devem ter aumentado.
7. Confirme no log `AUTOTESTE disparado`, `WakeLock parcial adquirido`, `Pulso ALARM registrado`, novo agendamento e `WakeLock liberado`.

Se o DND estiver ativo e a pausa de sono ligada, o log mostrará `Pulso pausado` e não incrementará o contador; esse é o comportamento esperado.

### Broadcasts úteis durante desenvolvimento

Simular reboot sem reiniciar (somente para validar manualmente a lógica; alguns dispositivos bloqueiam este broadcast via shell):

```bash
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p com.wakepulse.app
```

Limpar dados e repetir o onboarding:

```bash
adb shell pm clear com.wakepulse.app
```

O segundo comando apaga de forma irreversível as preferências e o histórico local do app.

## Procedimento comparativo de delayed notifications

Faça várias rodadas semelhantes, porque uma única mensagem não é evidência suficiente:

1. instale WakePulse;
2. permita alarmes exatos;
3. remova WakePulse da otimização de bateria, se aceitar o impacto;
4. escolha **9 minutos**;
5. ative WakePulse;
6. desligue a tela;
7. force Doze via ADB;
8. de outro aparelho, envie mensagens para apps como WhatsApp, Telegram ou Gmail;
9. anote horário de envio, recebimento e pulsos registrados;
10. execute `deviceidle unforce` ao final;
11. repita a mesma sequência com WakePulse desativado;
12. compare várias amostras e o consumo de bateria.

Não conclua que o WakePulse “garante” entrega. Google FCM, prioridade e implementação do app de destino, rede, servidor, fabricante e otimizações OEM podem alterar completamente o resultado. O pulso também não torna automaticamente a rede disponível para outros apps.

## Logs

Todos os eventos relevantes usam a tag `WakePulse`:

- agendamento, precisão e cancelamento;
- disparo do receiver;
- aquisição/liberação do WakeLock;
- reboot e restauração;
- mudança de intervalo e ativação;
- fallback e erros.

## Preparação para modo Anti-Doze com Shizuku

O modo normal não usa root, Shizuku, exploits nem comandos de shell. A separação entre `PulseController`, `PulseScheduleService` e gateways permite adicionar no futuro um componente independente, por exemplo `AntiDozeController`, sem misturá-lo ao caminho normal.

Uma futura implementação poderia, **somente após autorização Shizuku válida e ação explícita do usuário**, executar:

```text
dumpsys deviceidle disable
```

e restaurar obrigatoriamente com:

```text
dumpsys deviceidle enable
```

Esse modo deve exibir risco de bateria, manter estado de restauração, reativar o device idle ao desligar/revogar/sair e nunca assumir privilégios. Ele não está implementado nesta versão e nenhum comando root é executado.

## Privacidade e segurança de bateria

- todo estado fica no DataStore local;
- histórico limitado aos últimos 50 timestamps;
- WakeLock não referenciado, liberado em `finally` e protegido por timeout;
- execução serializada para rejeitar pulsos simultâneos no mesmo processo;
- scheduler protegido por `Mutex`;
- `PendingIntent` explícito, imutável e atualizado;
- alarme anterior cancelado antes do novo;
- nenhum scheduling automático se o usuário deixou o recurso desligado;
- nenhuma notificação é lida e nenhuma informação é transmitida.

### Medir impacto de bateria

Não estime por uma única noite. Faça duas janelas semelhantes de 24–48 horas, primeiro desativado e depois em 9 minutos, mantendo carga, sinal, Wi‑Fi e uso tão próximos quanto possível. Compare em **Configurações → Bateria → Uso desde a última carga** e anote o contador de pulsos. Como aproximação máxima sem throttling, um WakeLock de 5 segundos a cada 9 minutos soma ~33 segundos por hora; o custo dominante tende a ser acordar o SoC do deep sleep. Se o consumo for relevante, use 15 ou 30 minutos. A pausa durante Sono/DND reduz o custo noturno.

## Licença

O repositório não define uma licença de redistribuição. Adicione uma licença explícita antes de publicar ou aceitar contribuições externas.
