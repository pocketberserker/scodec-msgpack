package scodec.msgpack

import shapeless._
import scodec.{Attempt, Err}

object SerializeAuto extends TypeClassCompanion[Serialize] {
  override val typeClass: TypeClass[Serialize] =
    new SerializeTypeClassImpl()
}

private final class SerializeTypeClassImpl() extends TypeClass[Serialize] {

  override def coproduct[L, R <: Coproduct](CL: => Serialize[L], CR: => Serialize[R]) = {
    lazy val cl = CL
    lazy val cr = CR
    new Serialize[L :+: R] {
      override def pack(v: L :+: R) = v match {
        case Inl(l) => cl.pack(l)
        case Inr(r) => cr.pack(r)
      }
      override def unpack(v: MessagePack) =
        cl.unpack(v).map(Inl(_)).orElse(cr.unpack(v).map(Inr(_)))
    }
  }

  override val emptyCoproduct = new Serialize[CNil] {
    override def pack(v: CNil) = Attempt.successful(MNil)
    override def unpack(v: MessagePack) =
      Attempt.failure(Err(s"CNil does not have value."))
  }

  override val emptyProduct = new Serialize[HNil] {
    override def pack(v: HNil) = Attempt.successful(MNil)
    override def unpack(v: MessagePack) = v match {
      case MNil => Attempt.successful(HNil)
      case other => Attempt.failure(Err(s"expected MNil, but was $other."))
    }
  }

  override def product[H, T <: HList](H: Serialize[H], T: Serialize[T]) =
    new Serialize[H :: T] {
      override def pack(v: H :: T) = for {
        h <- H.pack(v.head)
        t <- T.pack(v.tail)
      } yield MFixArray(Vector(h, t))
      override def unpack(v: MessagePack) = v match {
        case MFixArray(Vector(v1, v2)) => for {
          h <- H.unpack(v1)
          t <- T.unpack(v2)
        } yield h :: t
        case other => Attempt.failure(Err(s"expected MfixArray(Vector(h, t)), but was $other."))
      }
    }

  override def project[F, G](instance: => Serialize[G], to: F => G, from: G => F) =
    instance.xmap(from, to)
}
