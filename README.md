# Jover IntelliJ Plugin

IntelliJ IDEA подавление ошибок, которые разрешает Jover (Java Overload).

## Список ситуаций подавления:
В случаях когда у левого операнда существует метод с требуемым аргументом

- `a + b` -> `a.add(b)`
- `a - b` -> `a.subtract(b)`
- `a * b` -> `a.multiply(b)`
- `a / b` -> `a.divide(b)`

Включая += *= /= -= операторы

## Требования
- IntelliJ IDEA 2026.1.3
