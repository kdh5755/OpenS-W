//9,10 = Back(IN1,IN2) , 11,12 = Back(IN3,IN4)
//6,5 = Foward(IN1,IN2) , 4,3 = Foward(IN3,IN4)
//2(F_L) , 7(F_R) = Forward(PWM) , 8(B_L),13(B_R) = Back(PWM)
//22,23 = Left UltraSonic trig,echo
//24,25 = Middle UltraSonic trig,echo
//26,27 = Right UltraSonic trig,echo

long L_distance, M_distance, R_distance;
long L_duration, M_duration, R_duration;

int F_L_ENABLE = 2;
int F_IN1 = 6; int F_IN2 = 5;
int F_IN3 = 4; int F_IN4 = 3;
int F_R_ENABLE = 7; 
int B_L_ENABLE = 8;
int B_IN1 = 9; int B_IN2 = 10;
int B_IN3 = 11; int B_IN4 = 12;
int B_R_ENABLE = 13;
int L_trig = 23; int L_echo = 22;
int M_trig = 25; int M_echo = 24;
int R_trig = 27; int R_echo = 26;

#define L_NEAR 55
#define L_FAR 60
#define M_NEAR 45
#define M_FAR 50
#define R_NEAR 55
#define R_FAR 60

void Forward();
void Left();
void Right();
void Back();
void Stop();
void avoid();
long L_Scan();
long M_Scan(); 
long R_Scan();

void setup() {
  Serial.begin(9600);
  pinMode(F_IN1, OUTPUT);
  pinMode(F_IN2, OUTPUT);
  pinMode(F_IN3, OUTPUT);
  pinMode(F_IN4, OUTPUT);
  pinMode(B_IN1, OUTPUT);
  pinMode(B_IN2, OUTPUT);
  pinMode(B_IN3, OUTPUT);
  pinMode(B_IN4, OUTPUT);
  pinMode(F_L_ENABLE, OUTPUT);
  pinMode(F_R_ENABLE, OUTPUT);
  pinMode(B_L_ENABLE, OUTPUT);
  pinMode(B_R_ENABLE, OUTPUT);
  pinMode(L_trig, OUTPUT);
  pinMode(L_echo, INPUT);
  pinMode(M_trig, OUTPUT);
  pinMode(M_echo, INPUT);
  pinMode(R_trig, OUTPUT);
  pinMode(R_echo, INPUT);
}

void loop() {
  if(Serial.available())
  {
    int result = (Serial.read() - '0');
    if(result == 1) 
    {
      Left();
      delay(200);
    }
    else if(result == 2)
    {
      Forward();
      delay(200);
    }
    else if(result == 3)
    {
      Right();
      delay(200);
    }
    else if(result == 4)
    {
      Forward();
      delay(200);
    }
    result = 4;
    
    L_Scan();
    M_Scan();
    R_Scan();
    if(L_distance <= L_NEAR || M_distance <= M_NEAR || R_distance <= R_NEAR)
    {
      avoid();
    }
  }
}

void Forward()
{
  digitalWrite(F_IN1, HIGH);
  digitalWrite(F_IN2, LOW);
  digitalWrite(F_IN3, LOW);
  digitalWrite(F_IN4, HIGH);
  analogWrite(F_L_ENABLE, 250);
  analogWrite(F_R_ENABLE, 250);
  digitalWrite(B_IN1, LOW);
  digitalWrite(B_IN2, HIGH);
  digitalWrite(B_IN3, HIGH);
  digitalWrite(B_IN4, LOW);
  analogWrite(B_L_ENABLE, 250);
  analogWrite(B_R_ENABLE, 250);
}
void Left()
{
  digitalWrite(F_IN1, LOW);
  digitalWrite(F_IN2, LOW);
  digitalWrite(F_IN3, LOW);
  digitalWrite(F_IN4, HIGH);
  analogWrite(F_L_ENABLE, 250);
  digitalWrite(B_IN1, LOW);
  digitalWrite(B_IN2, HIGH);
  digitalWrite(B_IN3, LOW);
  digitalWrite(B_IN4, LOW);
  analogWrite(B_L_ENABLE, 250);
}
void Right()
{
  digitalWrite(F_IN1, HIGH);
  digitalWrite(F_IN2, LOW);
  digitalWrite(F_IN3, LOW);
  digitalWrite(F_IN4, LOW);
  analogWrite(F_R_ENABLE, 250);
  digitalWrite(B_IN1, LOW);
  digitalWrite(B_IN2, LOW);
  digitalWrite(B_IN3, HIGH);
  digitalWrite(B_IN4, LOW);
  analogWrite(B_R_ENABLE, 250);
}
void Back()
{
  digitalWrite(F_IN1, LOW);
  digitalWrite(F_IN2, HIGH);
  digitalWrite(F_IN3, HIGH);
  digitalWrite(F_IN4, LOW);
  analogWrite(F_L_ENABLE, 250);
  analogWrite(F_R_ENABLE, 250);
  digitalWrite(B_IN1, HIGH);
  digitalWrite(B_IN2, LOW);
  digitalWrite(B_IN3, LOW);
  digitalWrite(B_IN4, HIGH);
  analogWrite(B_L_ENABLE, 250);
  analogWrite(B_R_ENABLE, 250);
}
void Stop()
{
  digitalWrite(F_IN1, LOW);
  digitalWrite(F_IN2, LOW);
  digitalWrite(F_IN3, LOW);
  digitalWrite(F_IN4, LOW);
  digitalWrite(B_IN1, LOW);
  digitalWrite(B_IN2, LOW);
  digitalWrite(B_IN3, LOW);
  digitalWrite(B_IN4, LOW);
}

void avoid()
{
  if(L_distance <= L_NEAR && M_distance <= M_NEAR && R_distance <= R_NEAR)
  {
    Stop();
  }
  else if(L_distance <= L_NEAR && M_distance <= M_NEAR && R_distance >= R_FAR)
  {
    Back();
    delay(200);
    Right();
    delay(400);
  }
  else if(L_distance <= L_NEAR && M_distance >= M_FAR && R_distance <= R_NEAR)
  {
    Forward();
  }
  else if(L_distance <= L_NEAR && M_distance >= M_FAR && R_distance >= R_FAR)
  {
    Forward();
    delay(200);
    Right();
    delay(400);
  }
  else if(L_distance >= L_FAR && M_distance <= M_NEAR && R_distance <= R_NEAR)
  {
    Back();
    delay(200);
    Left();
    delay(400);
  }
  else if(L_distance >= L_FAR && M_distance <= M_NEAR && R_distance >= R_FAR)
  {
    Back();
  }
  else if(L_distance >= L_FAR && M_distance >= M_FAR && R_distance <= R_NEAR)
  {
    Forward();
    delay(200);
    Left();
    delay(400);
  }
  else if(L_distance >= L_FAR && M_distance >= M_FAR && R_distance >= R_FAR)
  {
    Forward();
  }
}

long L_Scan()
{
  digitalWrite(L_trig, LOW);
  digitalWrite(L_echo, LOW);
  delayMicroseconds(2);
  digitalWrite(L_trig, HIGH);
  delayMicroseconds(5);
  digitalWrite(L_trig, LOW);
  L_duration = pulseIn(L_echo, HIGH);
  L_distance = ((float)(340 * L_duration) / 10000) / 2;
  if(L_distance < 10)
  {
    L_distance = 1000;
  }
  return L_distance;
}
long M_Scan()
{
  digitalWrite(M_trig, LOW);
  digitalWrite(M_echo, LOW);
  delayMicroseconds(2);
  digitalWrite(M_trig, HIGH);
  delayMicroseconds(5);
  digitalWrite(M_trig, LOW);
  M_duration = pulseIn(M_echo, HIGH);
  M_distance = ((float)(340 * M_duration) / 10000) / 2;
  if(L_distance < 10)
  {
    L_distance = 1000;
  }
  return M_distance;
}
long R_Scan()
{
  digitalWrite(R_trig, LOW);
  digitalWrite(R_echo, LOW);
  delayMicroseconds(2);
  digitalWrite(R_trig, HIGH);
  delayMicroseconds(5);
  digitalWrite(R_trig, LOW);
  R_duration = pulseIn(R_echo, HIGH);
  R_distance = ((float)(340 * R_duration) / 10000) / 2;
  if(L_distance < 10)
  {
    L_distance = 1000;
  }
  return R_distance;
}
