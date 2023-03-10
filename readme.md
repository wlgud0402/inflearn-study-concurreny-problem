# 동시성 처리

## syncronized
- 여러개의 서버를 운영하는 상황에서는 사용 불가
- 실질적으로 잘 사용되지 않음
- Transactional 어노테이션을 사용하는 메서드에 syncronized 를 붙여주어도 동시성을 해결할 수 없다.
  - 프록시 객체를 만들어 사용하게 되는데 생성된 해당 메서드의 경우 startTransaction 이 실제 메서드보다 먼저 실행되기 때문

## Mysql을 활용한 방법
1. 비관적락
   - 실제로 데이터에 Lock을 걸어서 정합성을 맞추는 방법
   - exclusive lock을 걸게되면 다른 트랜잭션에서는 lock이 해제되기 전에 데이터를 가져갈 수 없게 됨
   - 데드락이 걸릴 수 있기 때문에 주의하여 사용해야 함
2. 낙관적락 (버저닝)
   - 실제로 Lock을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법
   - 먼저 데이터를 읽은 후에 update 를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하여 업데이트
   - 내가 읽은 버전에서 수정사항이 생겼을 경우에는 application 에서 다시 읽은 후에 작업을 수행해야 합니다.
3. 네임드락
   - 이름을 가진 metadata locking 입니다.
   - 이름을 가진 lock 을 획등한 후 해제할때까지 다른 세션은 이 lock 을 획득할 수 없도록 함
   - 주의할점으로는 트랜잭션이 종료될 때 lock 이 자동으로 해제되지 않음
   - 별도의 명령어로 해제를 수행해주거나 선점시간이 끝나야 해제됨

## 비교
- 비관적락 vs 낙관적락
  - 비관적락
    - 실제로 락을 잡기 때문에 성능적인 측면 단점
    - 충돌이 잦을 경우 사용
  - 낙관적락
    - 별도의 락을 잡지 않기 때문에 성능상 이점
    - 실패시 재시도 및 롤백처리를 개발자가 해야함
    - 충돌이 적은 경우 사용
- 비관적락 vs 네임드락
  ![image](https://user-images.githubusercontent.com/61821825/210084690-5b3b9fb0-498a-4f47-8215-d614589f76f0.png)
  - 비관적락의 경우 접근하려는 데이터에 락을 거는 형태
  - 네임드락의 경우 별도의 공간에 락을 거는 형태
    - mysql (get_lock, release_lock)
    - 주로 분산락을 구현할 때 사용
    - 비관적락은 타임아웃을 구현하기 힘들지만 네임드락의 경우 손쉽게 구현이 가능
    - 트랜잭션 종료시 락 해제와 세션관리를 잘 해줘야 하며 실제 사용시 구현이 복잡할 수 있음

## redis 분산락 사용하기
- Lettuce
  - setnx 를 활용하여 분산락 구현 (구현이 간단함)
  - spin lock 방식
    - 락을 획득하려는 스레드가 락을 사용할수 있는지 반적으로 확인하면서 락 획득을 시도하는 방식
    - Redis 에 부담을 줄 수 있다. (Thread.sleep 을 통해 lock 획득에 텀을 두어 부담 줄여줄 수 있다.)
  - retry 로직을 개발자가 작성해야함
- Redisson
  - pub-sub 기반으로 Lock 구현 제공
    - redis의 부하를 줄여줄 수 있지만 구현이 조금 복잡
    - 별도의 redisson 라이브러리를 사용해야 한다는 단점
  - 채널을 만든후 락을 점유중인 스레드가 락 획득 대기중인 스레드에게 해제를 알려주면 안내를 받은 스레드가 락 획득을 시도하는 방식
  - 별도의 retry 로직 작성하지 않음


## Lettuce vs Redisson
- Lettuce
  - 구현이 간단
  - spring data redis를 이용하면 lettuce가 기본이기 때문에 별도의 라이브러리를 사용하지 않아도 됨
  - spin lock 방식이기 때문에 동시에 많은 스레드가 lock 획득 대기 상태라면 redis에 부하가 갈 수 있다
- Redisson
  - 락 획득 재시도를 기본으로 제공
  - pub-sub 방식으로 구현되어 있기 때문에 lettuce와 비교했을 때 reids에 부하가 덜 간다.
  - 별도의 라이브러리를 사용해야 한다.
  - lock 을 라이브러리 차원에서 제공해주기 때문에 사용법을 공부해야 한다.

- 실무에서는?
  - 재시도가 필요하지 않은 lock은 lettuce 활용
  - 재시도가 필요한 경우에는 redisson 활용


## Mysql vs Redis
- Mysql
  - 이미 Mysql을 사용하고 있다면 별도의 비용없이 사용가능
  - 어느정도의 트래픽까지는 문제없이 활용이 가능
  - Redis보다 성능이 딸림
- Redis
  - 활용중인 Redis가 없다면 별도의 구축비용과 인프라 관리비용이 발생
  - Mysql보다 성능이 좋다